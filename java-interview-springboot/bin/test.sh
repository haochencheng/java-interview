#! /bin/bash
MVN=$(which mvn)
echo ${MVN}
OUT_OF_MEMORY="-Dtest=pers.interview.springboot.controller.BigObjectControllerTest#addBigObject"

case $1 in
    outOfMemory)
        eval "${MVN} ${OUT_OF_MEMORY} test"
        ;;
      *)
        echo "Usage: $0 {outOfMemory}"
        exit 1
        ;;
esac

