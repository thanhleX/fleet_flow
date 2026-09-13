import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

// Custom metric counters for concurrency outcome
const successCounter = new Counter('dispatch_success_count');
const conflictCounter = new Counter('dispatch_conflict_count');

export const options = {
    scenarios: {
        concurrent_assign: {
            executor: 'shared-iterations',
            vus: 30,           // 30 concurrent staff members
            iterations: 30,    // hitting the exact same shipment at the same instant
            maxDuration: '10s',
        },
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const JWT_TOKEN = __ENV.TOKEN || 'YOUR_STAFF_JWT_TOKEN';
const TARGET_SHIPMENT_ID = __ENV.SHIPMENT_ID || '1';

export default function () {
    const url = `${BASE_URL}/api/v1/dispatch/${TARGET_SHIPMENT_ID}/assign`;

    const payload = JSON.stringify({
        driverId: 1,
        expectedVersion: 0,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${JWT_TOKEN}`,
        },
    };

    const res = http.post(url, payload, params);

    if (res.status === 200) {
        successCounter.add(1);
    } else if (res.status === 409 || res.status === 400) {
        conflictCounter.add(1);
    }

    check(res, {
        'response is handled correctly (200 success or 409/400 conflict)': (r) =>
            r.status === 200 || r.status === 409 || r.status === 400,
    });
}

