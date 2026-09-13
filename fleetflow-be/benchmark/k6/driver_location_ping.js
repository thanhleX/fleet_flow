import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 50 },
        { duration: '30s', target: 200 }, // 200 concurrent driver pings
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<150'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const JWT_TOKEN = __ENV.TOKEN || 'YOUR_DRIVER_JWT_TOKEN';

export default function () {
    const driverId = 1;
    const url = `${BASE_URL}/api/v1/drivers/${driverId}/location`;

    // Generate slight movement around Hanoi center
    const lat = 21.028511 + (Math.random() - 0.5) * 0.01;
    const lng = 105.804817 + (Math.random() - 0.5) * 0.01;
    const speed = 25.0 + Math.random() * 10;

    const payload = JSON.stringify({
        latitude: lat,
        longitude: lng,
        speed: speed,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${JWT_TOKEN}`,
        },
    };

    const res = http.post(url, payload, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(1); // Driver sends location every 1 second in stress test
}

