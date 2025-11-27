variable "project" {
  type    = string
  default = "restaurant-reservation"
}

variable "aws_region" {
  type    = string
  default = "ap-northeast-2"
}

variable "app_port" {
  type    = number
  default = 8080
}

variable "db_username" {
  type    = string
  default = "appuser"
}

variable "db_name" {
  type    = string
  default = "restaurant_reservation"
}

variable "desired_count" {
  type    = number
  default = 1
}
