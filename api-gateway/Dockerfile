FROM node:24-alpine
WORKDIR /workspace/api-gateway

COPY api-gateway/package*.json ./
RUN npm ci --omit=dev

COPY api-gateway/src ./src
COPY api-gateway/public ./public
COPY proto/contratos /workspace/proto/contratos

ENV NODE_ENV=production
ENV PORT=3000

EXPOSE 3000
CMD ["node", "src/server.js"]