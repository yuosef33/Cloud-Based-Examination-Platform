terraform {
  backend "s3" {
    bucket         = "yuosef33-terraform-state-2026"
    key            = "ec2/terraform.tfstate"
    region         = "eu-central-1"
    dynamodb_table = "terraform-state-locking"
    encrypt        = true
  }
}
provider "aws" {
  region = "eu-central-1"
}

resource "aws_instance" "demo_ec2" {
  ami           = "ami-0191d47ba10441f0b"
  instance_type = "t2.micro"

  tags = {
    Name = "terraform-from-spring"
  }
}