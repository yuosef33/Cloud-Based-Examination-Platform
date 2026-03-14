terraform {
  backend "s3" {
    bucket         = "yuosef33-terraform-state-2026"
    region         = "eu-central-1"
    dynamodb_table = "terraform-state-locking"
    encrypt        = true
  }
}

provider "aws" {
  region = "eu-central-1"
}

# variables
variable "ami_id" {
  type = string
  default = ""
}

variable "instance_type" {
  type = string
  default = ""
}

variable "instance_name" {
  type = string
  default = ""
}

# ec2 resource
resource "aws_instance" "lab_vm" {

  ami           = var.ami_id
  instance_type = var.instance_type
  key_name      = "Ec2-Base"
  vpc_security_group_ids = ["sg-0687cc28f63bd584a"]

  tags = {
    Name = var.instance_name
  }

}

# outputs
output "public_ip" {
  value = aws_instance.lab_vm.public_ip
}

output "instance_id" {
  value = aws_instance.lab_vm.id
}