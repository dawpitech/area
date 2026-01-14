#!/usr/bin/env sh
sed -i 's|REACT_APP_API_URL_PLACEHOLDER|'${REACT_APP_API_URL}'|g' /usr/share/nginx/html/static/js/*.js

cp /apk/client.apk public/. || true

nginx -g 'daemon off;'
