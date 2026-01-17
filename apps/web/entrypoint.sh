#!/usr/bin/env sh
sed -i 's|REACT_APP_API_URL_PLACEHOLDER|'${REACT_APP_API_URL}'|g' /usr/share/nginx/html/static/js/*.js

nginx -g 'daemon off;'
