import http from 'k6/http';
import { check } from 'k6';

export const options = {
    scenarios: {
        idempotency_retry: {
            executor: 'shared-iterations',
            vus: 10,          // 10 concurrent duplicated submissions with the exact same Idempotency-Key
            iterations: 10,
            maxDuration: '5s',
        },
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const JWT_TOKEN = __ENV.TOKEN || 'YOUR_STAFF_JWT_TOKEN';
// Shared static idempotency key across all concurrent requests
const FIXED_IDEMPOTENCY_KEY = 'IDEM-BENCHMARK-TEST-KEY-001';

export default function () {
    const url = `${BASE_URL}/api/v1/cod/settle`;

    const payload = JSON.stringify({
        driverId: 1,
        idempotencyKey: FIXED_IDEMPOTENCY_KEY,
        note: 'Load test idempotent settlement',
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${JWT_TOKEN}`,
            'Idempotency-Key': FIXED_IDEMPOTENCY_KEY,
        },
    };

    const res = http.post(url, payload, params);

    // All duplicated requests must succeed (200 OK returning the same settlement) or return conflict handled gracefully
    check(res, {
        'idempotency is upheld (status 200)': (r) => r.status === 200,
    });
}

