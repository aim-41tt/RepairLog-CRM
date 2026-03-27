# ════════════════════════════════════════════════════════════════════════════
# CRM RepairLog Frontend — Dev Dockerfile
# Angular 18 dev server
# ════════════════════════════════════════════════════════════════════════════
FROM node:20-alpine

WORKDIR /app

# Сначала копируем package*.json — npm ci закеширует зависимости
COPY package.json package-lock.json ./
RUN npm ci

# Копируем исходники
COPY . .

EXPOSE 4200

CMD ["npx", "ng", "serve", "--host", "0.0.0.0", "--disable-host-check"]
