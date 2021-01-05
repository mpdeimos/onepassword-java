#!/bin/bash
# Signs into 1password with the same credentials as stored in .test.env.

if [ "${BASH_SOURCE[0]}" -ef "$0" ]
then
	echo "This script needs to be sourced"
	exit 1
fi

source .test.env
[ -z "$OP_TEST_DEVICE" ] && export OP_TEST_DEVICE=`head -c 16 /dev/urandom | base32 | tr -d = | tr '[:upper:]' '[:lower:]'`
export OP_DEVICE="$OP_TEST_DEVICE"
eval `echo $OP_TEST_PASSWORD | ./build/bin/op signin $OP_TEST_SIGNINADDRESS $OP_TEST_EMAILADDRESS $OP_TEST_SECRETKEY`