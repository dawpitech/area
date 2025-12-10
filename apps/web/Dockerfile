FROM node:22.18.0-alpine
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

EXPOSE 3000

CMD ["sh", "-c", "cp /apk/client.apk public/. ; npm start"]
