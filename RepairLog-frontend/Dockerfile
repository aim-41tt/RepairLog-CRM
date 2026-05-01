# ════════════════════════════════════════════════════════════════════════════
# CRM RepairLog Frontend — Production Dockerfile
# Stage 1: build Angular app (production)
# Stage 2: serve with nginx
# ════════════════════════════════════════════════════════════════════════════

# ── Stage 1: Builder ────────────────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# Cache npm dependencies separately
COPY package.json package-lock.json ./
RUN npm ci --ignore-scripts

# Copy sources and build
COPY . .
RUN npm run build -- --configuration production

# ── Stage 2: Runtime (nginx) ─────────────────────────────────────────────────
FROM nginx:1.27-alpine AS runtime

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/repairlog.conf

# Copy production build artifacts
# Angular 18 outputs to dist/<project>/browser/
COPY --from=builder /app/dist/repair-log-frontend/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
