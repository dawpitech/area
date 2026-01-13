FROM node:22.18.0-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80

CMD ["sh", "-c", "cp /apk/client.apk public/. ; nginx -g 'daemon off;'"]
