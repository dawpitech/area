FROM node:22.18.0-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
CMD ["sh", "-c", "echo 'REACT_APP_API_URL=REACT_APP_API_URL_PLACEHOLDER' > .env"]
RUN npm run build

FROM nginx:alpine

COPY --from=builder /app/build /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 80

CMD ["sh", "-c", "/entrypoint.sh"]
