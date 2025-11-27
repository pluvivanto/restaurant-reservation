terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

data "aws_availability_zones" "available" {}

module "vpc" {
  source = "terraform-aws-modules/vpc/aws"

  name = "${var.project}-vpc"
  cidr = "10.0.0.0/16"

  azs             = slice(data.aws_availability_zones.available.names, 0, 2)
  public_subnets  = ["10.0.0.0/20", "10.0.16.0/20"]
  private_subnets = ["10.0.128.0/20", "10.0.144.0/20"]

  enable_dns_hostnames = true
  enable_dns_support   = true
  enable_nat_gateway   = false

  public_subnet_tags  = { Tier = "public" }
  private_subnet_tags = { Tier = "private" }
}

module "alb_sg" {
  source = "terraform-aws-modules/security-group/aws"

  name   = "${var.project}-alb-sg"
  vpc_id = module.vpc.vpc_id

  ingress_rules       = ["http-80-tcp"]
  ingress_cidr_blocks = ["0.0.0.0/0"]
  egress_rules        = ["all-all"]
}

module "ecs_sg" {
  source = "terraform-aws-modules/security-group/aws"

  name   = "${var.project}-ecs-sg"
  vpc_id = module.vpc.vpc_id

  ingress_with_source_security_group_id = [{
    from_port                = var.app_port
    to_port                  = var.app_port
    protocol                 = "tcp"
    source_security_group_id = module.alb_sg.security_group_id
  }]

  egress_rules = ["all-all"]
}

module "db_sg" {
  source = "terraform-aws-modules/security-group/aws"

  name   = "${var.project}-db-sg"
  vpc_id = module.vpc.vpc_id

  ingress_with_source_security_group_id = [{
    from_port                = 5432
    to_port                  = 5432
    protocol                 = "tcp"
    source_security_group_id = module.ecs_sg.security_group_id
  }]

  egress_rules = ["all-all"]
}

module "db" {
  source = "terraform-aws-modules/rds/aws"

  identifier = "${var.project}-db"

  engine                      = "postgres"
  engine_version              = "17"
  instance_class              = "db.t4g.micro"
  allocated_storage           = 20
  db_name                     = var.db_name
  username                    = var.db_username
  manage_master_user_password = true
  family                      = "postgres17"
  port                        = 5432
  skip_final_snapshot         = true
  publicly_accessible         = false
  apply_immediately           = true
  auto_minor_version_upgrade  = false

  db_subnet_group_name   = null
  create_db_subnet_group = true
  subnet_ids             = module.vpc.private_subnets

  vpc_security_group_ids = [module.db_sg.security_group_id]
}

module "alb" {
  source = "terraform-aws-modules/alb/aws"

  name               = "${var.project}-alb"
  load_balancer_type = "application"
  vpc_id             = module.vpc.vpc_id
  subnets            = module.vpc.public_subnets
  security_groups    = [module.alb_sg.security_group_id]

  target_groups = {
    app = {
      create_attachment = false
      target_type       = "ip"
      backend_protocol  = "HTTP"
      backend_port      = var.app_port
      health_check = {
        enabled             = true
        healthy_threshold   = 2
        unhealthy_threshold = 5
        interval            = 30
        timeout             = 5
        matcher             = "200-399"
        path                = "/actuator/health"
      }
    }
  }

  listeners = {
    http = {
      port     = 80
      protocol = "HTTP"
      forward = {
        target_group_key = "app"
      }
    }
  }
}

resource "aws_ecs_cluster" "main" {
  name = "${var.project}-cluster"
}

resource "aws_iam_role" "ecs_task_execution" {
  name = "${var.project}-task-exec"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "ecs_task_execution_secrets" {
  name = "${var.project}-task-exec-secrets"
  role = aws_iam_role.ecs_task_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["secretsmanager:GetSecretValue", "secretsmanager:DescribeSecret"]
        Resource = module.db.db_instance_master_user_secret_arn
      }
    ]
  })
}

resource "aws_ecr_repository" "app" {
  name                 = var.project
  image_tag_mutability = "MUTABLE"
  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecr_lifecycle_policy" "app" {
  repository = aws_ecr_repository.app.name
  policy     = jsonencode({ rules = [{ rulePriority = 1, selection = { tagStatus = "any", countType = "imageCountMoreThan", countNumber = 10 }, action = { type = "expire" } }] })
}

resource "aws_cloudwatch_log_group" "ecs" {
  name              = "/ecs/${var.project}"
  retention_in_days = 1
}

resource "aws_ecs_task_definition" "app" {
  family                   = "${var.project}-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task_execution.arn

  container_definitions = jsonencode([
    {
      name      = "app"
      image     = "${aws_ecr_repository.app.repository_url}:latest"
      essential = true
      portMappings = [{
        containerPort = var.app_port
        hostPort      = var.app_port
        protocol      = "tcp"
      }]
      environment = [
        { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://${module.db.db_instance_address}:5432/${var.db_name}" },
        { name = "SPRING_DATASOURCE_USERNAME", value = var.db_username }
      ]
      secrets = [{
        name      = "SPRING_DATASOURCE_PASSWORD"
        valueFrom = "${module.db.db_instance_master_user_secret_arn}:password::"
      }]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.ecs.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "app"
        }
      }
    }
  ])
}

resource "aws_ecs_service" "app" {
  name            = "${var.project}-svc"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = module.vpc.public_subnets
    security_groups  = [module.ecs_sg.security_group_id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = module.alb.target_groups["app"].arn
    container_name   = "app"
    container_port   = var.app_port
  }

  depends_on = [module.alb]
}

output "alb_dns_name" {
  value = module.alb.dns_name
}

output "db_endpoint" {
  value = module.db.db_instance_address
}

output "db_credentials" {
  value     = { username = var.db_username, password_secret_arn = module.db.db_instance_master_user_secret_arn }
  sensitive = true
}

output "ecr_repository_url" {
  value = aws_ecr_repository.app.repository_url
}
