#!/bin/bash
set -e

GET_CONFIG="/opt/elasticbeanstalk/bin/get-config"

CERTBOT_IP="$($GET_CONFIG environment -k CERTBOT_IP)"
CERTBOT_EMAIL="$($GET_CONFIG environment -k CERTBOT_EMAIL)"

if [ -z "$CERTBOT_IP" ] || [ -z "$CERTBOT_EMAIL" ]; then
    echo "ERROR: CERTBOT_IP o CERTBOT_EMAIL no están configurados."
    exit 1
fi

CERT_PATH="/etc/letsencrypt/live/${CERTBOT_IP}"

if [ ! -f "${CERT_PATH}/fullchain.pem" ]; then
    certbot certonly \
        --preferred-profile shortlived \
        --webroot \
        --webroot-path /var/www/letsencrypt \
        --ip-address "$CERTBOT_IP" \
        --non-interactive \
        --agree-tos \
        --email "$CERTBOT_EMAIL"
fi

cat > /etc/nginx/conf.d/https.conf <<EOF
server {
    listen 443 ssl;
    server_name ${CERTBOT_IP};

    ssl_certificate ${CERT_PATH}/fullchain.pem;
    ssl_certificate_key ${CERT_PATH}/privkey.pem;

    ssl_protocols TLSv1.2 TLSv1.3;

    include /etc/nginx/conf.d/elasticbeanstalk/*.conf;
}
EOF

nginx -t
systemctl reload nginx

echo "HTTPS activado correctamente."