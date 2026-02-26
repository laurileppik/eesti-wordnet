FROM node:22 AS build
WORKDIR /app
ARG BUILD_CONFIG=production
COPY package.json package-lock.json* ./
RUN npm ci --silent
COPY . .
RUN npm run build -- --configuration ${BUILD_CONFIG}

FROM nginx:alpine
COPY nginx-default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/eesti-wordnet-fe/browser/ /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
