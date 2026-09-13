import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '15s', target: 20 },  // Ramp-up to 20 users
        { duration: '30s', target: 100 }, // Peak at 100 concurrent users
        { duration: '15s', target: 0 },   // Cool-down
    ],
    thresholds: {
        http_req_duration: ['p(95)<200', 'p(99)<500'], // 95% requests must be faster than 200ms
        http_req_failed: ['rate<0.01'],                 // Error rate < 1%
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const SAMPLE_TRACKING_CODES = ['FF00000001', 'FF00000002', 'FF00000003', 'FF00000004'];

export default function () {
    const randomCode = SAMPLE_TRACKING_CODES[Math.floor(Math.random() * SAMPLE_TRACKING_CODES.length)];
    const url = `${BASE_URL}/api/v1/public/tracking/${randomCode}`;

    const res = http.get(url, {
        headers: { 'Accept': 'application/json' },
    });

    check(res, {
        'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
        'response time < 250ms': (r) => r.timings.duration < 250,
    });

    sleep(0.05); // Short pacing between queries
}

