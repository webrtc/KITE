#! /bin/bash

cd "/KITE/${KITE_TEST_PATH}/js"
rm -rf node_modules
npm install

cd /KITE/${KITE_TEST_PATH}

/KITE/r "configs/${KITE_CONFIG_NAME}"
