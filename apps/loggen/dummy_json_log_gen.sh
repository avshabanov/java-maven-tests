# !/bin/bash

# Dummy structured log generator
# Sample use:
#   bash dummy_json_log_gen.sh | jq

while true
do
  DATE=$(echo `date +%Y-%m-%d\ %T.%Z`)
  # GUID:
  #ID=$(od -x /dev/urandom | head -1 | awk '{OFS="-"; print $2$3,$4,$5,$6,$7$8$9}')
  ID=$(od -x /dev/urandom | head -1 | awk '{OFS=""; print $2$3}')
  MSG="Test log message $(uuidgen)"
  SYSTEM_DATA=$(top -bn1 | head -8 | tail -1)
  SYSTEM_INFO=$(echo $SYSTEM_DATA | awk '{OFS=" "; print "\"pid\": "$1", \"user\": \""$2"\", \"cpu\": "$9", \"virt\": \""$5"\", \"mem\": \""$10"\", \"time-delta\": \""$11"\""}')

  echo "{\"date\": \"$DATE\", \"msg\": \"$MSG\", \"request\": \"$ID\", $SYSTEM_INFO}"

  sleep $(echo $(($(shuf -i 1-200 -n 1)/100)).$(($(shuf -i 1-100 -n 1))))
done

exit 0
