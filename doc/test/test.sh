echo "This script is to run the test!"
echo ""
echo "Make sure that you have configure AWS access key!"
echo ""
echo "Create a key-pair 677kp if you haven't"
echo ""
echo "If you encounter any issue with running aws, try troubleshooting with this https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/TroubleshootingInstancesConnecting.html#TroubleshootingInstancesConnectingSSH"
echo ""
echo "Let's start!"

# Step 1: Create instance
# Save all the EC2 PrivateDnsName to a file
# Step 2: git clone the project
# Compile the project
# Run the project

## Start an EC2 instance
#aws ec2 run-instances --image-id ami-0fc61db8544a617ed --instance-type t2.micro --key-name 677kp > instance.json
#
## Get instance Id
#
#aws ec2 describe-instances --instance-id $InstanceId > runningInstance.json
#
#PublicDnsName=$(grep -m 1 '^ *"PublicDnsName":' runningInstance.json | awk '{ print $2 }' | sed -e 's/,$//' -e 's/^"//' -e 's/"$//')
#
#scp -i "677kp.pem" Config.txt ec2-user@$PublicDnsName
#
#ssh -i "677kp.pem" ec2-user@$PublicDnsName
#
#./gradlew clean build

InstanceId=$(grep '^ *"InstanceId":' instance.json | awk '{ print $2 }' | sed -e 's/,$//' -e 's/^"//' -e 's/"$//')
aws ec2 describe-instances --instance-id $InstanceId
ssh -i "677kp.pem" ec2-user@ec2-54-147-62-56.compute-1.amazonaws.com
