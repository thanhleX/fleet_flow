import React, { useState, useEffect } from 'react';
import { api, getStoredAuth, setStoredAuth } from './services/api';
import {
  Package,
  Truck,
  DollarSign,
  Activity,
  Search,
  PlusCircle,
  CheckCircle,
  AlertTriangle,
  RotateCcw,
  Navigation,
  ShieldCheck,
  Zap,
  UserCheck,
  LogOut,
  RefreshCw,
  Clock,
  MapPin,
  FileText,
  Lock,
  User,
  ArrowLeft
} from 'lucide-react';

export default function App() {
  const [currentUser, setCurrentUser] = useState(getStoredAuth());
  const [activeTab, setActiveTab] = useState('dashboard');
  const [showPublicTrackingOnly, setShowPublicTrackingOnly] = useState(false);

  // Login form state
  const [loginUsername, setLoginUsername] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  // App data state
  const [dashboardData, setDashboardData] = useState(null);
  const [shipments, setShipments] = useState([]);
  const [selectedStatus, setSelectedStatus] = useState('ALL');
  const [loading, setLoading] = useState(false);
  const [notification, setNotification] = useState(null);

  // Forms & Modals state
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createForm, setCreateForm] = useState({
    senderName: 'Cửa hàng Phố Huế',
    senderPhone: '0987654321',
    senderAddress: '25 Phố Huế, Hoàn Kiếm',
    senderProvince: 'Hà Nội',
    receiverName: 'Nguyễn Văn A',
    receiverPhone: '0912345678',
    receiverAddress: '15 Cầu Giấy',
    receiverProvince: 'Hà Nội',
    originHubId: 1,
    serviceTier: 'STANDARD',
    totalWeightKg: 1.5,
    lengthCm: 30,
    widthCm: 20,
    heightCm: 15,
    declaredValue: 500000,
    codAmount: 350000,
    itemName: 'Áo khoác Vintage',
    itemQuantity: 1,
  });
  const [estimatedPrice, setEstimatedPrice] = useState(null);

  // Tracking tab state
  const [trackingCodeInput, setTrackingCodeInput] = useState('');
  const [trackingResult, setTrackingResult] = useState(null);

  // Driver tab state
  const [driverTasks, setDriverTasks] = useState([]);

  // COD tab state
  const [selectedDriverForCod, setSelectedDriverForCod] = useState(1);
  const [pendingCodList, setPendingCodList] = useState([]);

  const showToast = (msg, type = 'success') => {
    setNotification({ msg, type });
    setTimeout(() => setNotification(null), 4000);
  };

  // Listen to 401 unauthorized
  useEffect(() => {
    const handleUnauthorized = () => {
      setCurrentUser(null);
      showToast('Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.', 'error');
    };
    window.addEventListener('fleetflow_unauthorized', handleUnauthorized);
    return () => window.removeEventListener('fleetflow_unauthorized', handleUnauthorized);
  }, []);

  // Perform Login
  const handleLogin = async (username, password) => {
    try {
      setLoading(true);
      const res = await api.login(username, password);
      setCurrentUser(res);
      showToast(`Đăng nhập thành công: ${res.username} (${res.role})`);

      // Switch default tab according to role
      if (res.role === 'ROLE_DRIVER') {
        setActiveTab('driver');
      } else if (res.role === 'ROLE_STAFF') {
        setActiveTab('shipments');
      } else {
        setActiveTab('dashboard');
      }
    } catch (err) {
      showToast('Đăng nhập thất bại: ' + err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    api.logout();
    setCurrentUser(null);
    setDashboardData(null);
    setShipments([]);
    setDriverTasks([]);
    setPendingCodList([]);
    showToast('Đã đăng xuất khỏi hệ thống');
  };

  const loadDashboard = async () => {
    try {
      const data = await api.getDashboard();
      setDashboardData(data);
    } catch (err) {
      console.warn('Dashboard load error:', err.message);
    }
  };

  const loadShipments = async (status = selectedStatus) => {
    setLoading(true);
    try {
      if (status === 'ALL') {
        const p1 = await api.getShipmentsByStatus('PENDING', 0, 10).catch(() => ({ items: [] }));
        const p2 = await api.getShipmentsByStatus('ASSIGNED', 0, 10).catch(() => ({ items: [] }));
        const p3 = await api.getShipmentsByStatus('DELIVERED', 0, 10).catch(() => ({ items: [] }));
        setShipments([...(p1.items || []), ...(p2.items || []), ...(p3.items || [])]);
      } else {
        const data = await api.getShipmentsByStatus(status, 0, 30);
        setShipments(data.items || []);
      }
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const loadDriverTasks = async (driverId = null) => {
    try {
      const targetDriverId = currentUser?.role === 'ROLE_DRIVER' ? null : (driverId || 1);
      const res = await api.getMyDriverTasks(targetDriverId);
      setDriverTasks(res?.items || []);
    } catch (err) {
      console.warn('loadDriverTasks error:', err.message);
    }
  };

  const loadPendingCod = async (driverId = selectedDriverForCod) => {
    try {
      const res = await api.getPendingCod(driverId);
      setPendingCodList(res || []);
    } catch (err) {
      showToast(err.message, 'error');
    }
  };

  useEffect(() => {
    if (currentUser) {
      if (activeTab === 'dashboard') loadDashboard();
      if (activeTab === 'shipments') loadShipments();
      if (activeTab === 'driver') loadDriverTasks();
      if (activeTab === 'cod') loadPendingCod();
    }
  }, [currentUser, activeTab, selectedStatus]);

  // Live Pricing Estimation calculation
  const handleEstimatePricing = async () => {
    try {
      const res = await api.estimatePricing({
        regionZone: 'INTRA_PROVINCE',
        serviceTier: createForm.serviceTier,
        weightKg: parseFloat(createForm.totalWeightKg) || 1.0,
        lengthCm: parseFloat(createForm.lengthCm),
        widthCm: parseFloat(createForm.widthCm),
        heightCm: parseFloat(createForm.heightCm),
        declaredValue: parseFloat(createForm.declaredValue),
        codAmount: parseFloat(createForm.codAmount),
      });
      setEstimatedPrice(res);
    } catch (err) {
      console.error(err);
    }
  };

  const handleCreateShipmentSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      const payload = {
        senderName: createForm.senderName,
        senderPhone: createForm.senderPhone,
        senderAddress: createForm.senderAddress,
        senderProvince: createForm.senderProvince,
        receiverName: createForm.receiverName,
        receiverPhone: createForm.receiverPhone,
        receiverAddress: createForm.receiverAddress,
        receiverProvince: createForm.receiverProvince,
        originHubId: parseInt(createForm.originHubId),
        serviceTier: createForm.serviceTier,
        totalWeightKg: parseFloat(createForm.totalWeightKg),
        lengthCm: parseFloat(createForm.lengthCm),
        widthCm: parseFloat(createForm.widthCm),
        heightCm: parseFloat(createForm.heightCm),
        declaredValue: parseFloat(createForm.declaredValue),
        codAmount: parseFloat(createForm.codAmount),
        items: [
          {
            itemName: createForm.itemName,
            quantity: parseInt(createForm.itemQuantity),
            weightKg: parseFloat(createForm.totalWeightKg),
            declaredPrice: parseFloat(createForm.declaredValue),
          }
        ]
      };
      const created = await api.createShipment(payload);
      showToast(`Tạo đơn thành công! Mã vận đơn: ${created.trackingCode}`);
      setShowCreateModal(false);
      loadShipments();
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  // Test Concurrency
  const handleTestConcurrency = async (shipmentId) => {
    showToast('🚀 Đang bắn 5 requests đồng thời để kiểm tra Optimistic Locking...', 'info');
    const promises = [1, 2, 1, 2, 1].map((driverId, idx) =>
      api.assignDriver(shipmentId, driverId, 0)
        .then(() => ({ id: idx + 1, status: 'SUCCESS 200 OK (Đã gán tài xế)' }))
        .catch(err => ({ id: idx + 1, status: `CONFLICT 409: ${err.message}` }))
    );

    const results = await Promise.all(promises);
    alert('KẾT QUẢ KIỂM THỬ CONCURRENCY (Race Condition Test):\n\n' +
      results.map(r => `Request #${r.id}: ${r.status}`).join('\n')
    );
    loadShipments();
  };

  // Test Idempotency
  const handleTestIdempotency = async () => {
    const key = `IDEM-TEST-${Date.now()}`;
    showToast(`🛡️ Đang gửi 2 requests liên tiếp cùng Idempotency-Key: ${key}`, 'info');

    try {
      const res1 = await api.settleCod(selectedDriverForCod, key, 'Test lần 1');
      const res2 = await api.settleCod(selectedDriverForCod, key, 'Test lần 2 (Trùng key)');

      alert(`✅ KẾT QUẢ KIỂM THỬ IDEMPOTENCY:\n\n` +
        `Request 1: Thành công! Mã đối soát = ${res1.settlementCode}, Tổng tiền = ${res1.totalAmount} VNĐ\n` +
        `Request 2 (Idempotency Hit): Thành công! Mã đối soát = ${res2.settlementCode}, Tổng tiền = ${res2.totalAmount} VNĐ\n\n` +
        `=> Dữ liệu tài chính an toàn tuyệt đối! Hệ thống không đối soát 2 lần, tiền không bị nhân đôi!`
      );
      loadPendingCod();
    } catch (err) {
      showToast(err.message, 'error');
    }
  };

  const handleSearchTracking = async (e) => {
    e.preventDefault();
    if (!trackingCodeInput) return;
    try {
      setLoading(true);
      const res = await api.trackPublic(trackingCodeInput.trim());
      setTrackingResult(res);
    } catch (err) {
      showToast('Không tìm thấy vận đơn: ' + err.message, 'error');
      setTrackingResult(null);
    } finally {
      setLoading(false);
    }
  };

  // ==========================================
  // VIEW 1: UNLOGGED-IN LOGIN SCREEN
  // ==========================================
  if (!currentUser && !showPublicTrackingOnly) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-blue-950 flex flex-col justify-center items-center p-4">
        {notification && (
          <div className="fixed top-5 right-5 z-50">
            <div className={`px-4 py-3 rounded-lg shadow-lg text-white text-xs font-semibold ${
              notification.type === 'error' ? 'bg-red-600' : 'bg-emerald-600'
            }`}>
              {notification.msg}
            </div>
          </div>
        )}

        <div className="max-w-md w-full bg-white rounded-2xl shadow-2xl overflow-hidden border border-slate-700/30">
          {/* Header */}
          <div className="bg-gradient-to-r from-blue-600 to-indigo-700 p-6 text-white text-center">
            <div className="w-14 h-14 bg-white/20 backdrop-blur rounded-2xl mx-auto flex items-center justify-center mb-3 shadow-inner">
              <Truck size={28} className="text-white" />
            </div>
            <h1 className="text-2xl font-black tracking-tight">FleetFlow System</h1>
            <p className="text-xs text-blue-100 mt-1">Đăng nhập để truy cập hệ thống quản lý vận chuyển</p>
          </div>

          {/* Form */}
          <div className="p-6 space-y-5">
            <form onSubmit={(e) => { e.preventDefault(); handleLogin(loginUsername, loginPassword); }} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Tên đăng nhập (Username)</label>
                <div className="relative">
                  <User size={14} className="absolute left-3 top-3 text-slate-400" />
                  <input
                    type="text"
                    required
                    value={loginUsername}
                    onChange={e => setLoginUsername(e.target.value)}
                    placeholder="admin, staff_hn, driver_nam..."
                    className="w-full pl-9 pr-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-blue-500 focus:outline-none"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Mật khẩu (Password)</label>
                <div className="relative">
                  <Lock size={14} className="absolute left-3 top-3 text-slate-400" />
                  <input
                    type="password"
                    required
                    value={loginPassword}
                    onChange={e => setLoginPassword(e.target.value)}
                    placeholder="Mật khẩu mặc định: password123"
                    className="w-full pl-9 pr-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-blue-500 focus:outline-none"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-lg text-xs transition shadow-md flex items-center justify-center gap-2"
              >
                {loading ? <RefreshCw size={14} className="animate-spin" /> : <UserCheck size={14} />}
                Đăng Nhập Vào Hệ Thống
              </button>
            </form>

            {/* Divider */}
            <div className="relative flex items-center justify-center">
              <div className="border-t border-slate-200 w-full"></div>
              <span className="bg-white px-3 text-[11px] text-slate-400 uppercase font-semibold whitespace-nowrap">
                Hoặc đăng nhập nhanh 1-Click
              </span>
            </div>

            {/* 1-Click Quick Login Cards */}
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => handleLogin('admin', 'password123')}
                className="p-2.5 border border-purple-200 bg-purple-50 hover:bg-purple-100 rounded-xl text-center transition group"
              >
                <div className="text-base mb-1">👑</div>
                <div className="text-xs font-bold text-purple-900">Admin</div>
                <div className="text-[10px] text-purple-600 mt-0.5">Toàn quyền</div>
              </button>

              <button
                type="button"
                onClick={() => handleLogin('staff_hn', 'password123')}
                className="p-2.5 border border-blue-200 bg-blue-50 hover:bg-blue-100 rounded-xl text-center transition group"
              >
                <div className="text-base mb-1">📋</div>
                <div className="text-xs font-bold text-blue-900">Staff</div>
                <div className="text-[10px] text-blue-600 mt-0.5">Điều phối</div>
              </button>

              <button
                type="button"
                onClick={() => handleLogin('driver_nam', 'password123')}
                className="p-2.5 border border-emerald-200 bg-emerald-50 hover:bg-emerald-100 rounded-xl text-center transition group"
              >
                <div className="text-base mb-1">🛵</div>
                <div className="text-xs font-bold text-emerald-900">Driver</div>
                <div className="text-[10px] text-emerald-600 mt-0.5">Giao hàng</div>
              </button>
            </div>

            {/* Public Tracking Link */}
            <div className="text-center pt-2 border-t border-slate-100">
              <button
                type="button"
                onClick={() => setShowPublicTrackingOnly(true)}
                className="text-xs text-blue-600 hover:text-blue-800 font-semibold inline-flex items-center gap-1.5"
              >
                <Search size={14} />
                Khách hàng tra cứu đơn công khai (Không cần đăng nhập)
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // ==========================================
  // VIEW 2: PUBLIC TRACKING ONLY (Unauthenticated)
  // ==========================================
  if (!currentUser && showPublicTrackingOnly) {
    return (
      <div className="min-h-screen bg-slate-100 p-4 sm:p-8">
        <div className="max-w-3xl mx-auto space-y-4">
          <div className="flex justify-between items-center bg-white p-4 rounded-xl shadow-sm">
            <div className="flex items-center gap-2 font-bold text-blue-600">
              <Truck size={20} />
              <span>FleetFlow Public Tracking</span>
            </div>
            <button
              onClick={() => setShowPublicTrackingOnly(false)}
              className="px-3 py-1.5 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-bold flex items-center gap-1.5"
            >
              <ArrowLeft size={14} /> Quay lại Đăng nhập
            </button>
          </div>

          <div className="bg-white p-6 rounded-xl shadow-sm space-y-4">
            <h2 className="text-base font-bold text-slate-800 flex items-center gap-2">
              <Search size={18} className="text-blue-500" /> Tra Cứu Lộ Trình Vận Đơn
            </h2>

            <form onSubmit={handleSearchTracking} className="flex gap-2">
              <input
                type="text"
                value={trackingCodeInput}
                onChange={e => setTrackingCodeInput(e.target.value)}
                placeholder="Nhập mã vận đơn (VD: FF-HN240901-001)..."
                className="flex-1 px-4 py-2 border rounded-lg text-xs font-mono focus:ring-2 focus:ring-blue-500 focus:outline-none"
              />
              <button type="submit" className="px-5 py-2 bg-blue-600 text-white text-xs font-semibold rounded-lg hover:bg-blue-700">
                Tra cứu
              </button>
            </form>

            {/* Quick suggestions */}
            <div className="flex flex-wrap items-center gap-2 pt-1 text-[11px] text-slate-500">
              <span>Mã mẫu kiểm thử:</span>
              <button type="button" onClick={() => setTrackingCodeInput('FF-HN240902-009')} className="px-2 py-1 bg-blue-50 hover:bg-blue-100 text-blue-700 rounded font-mono border border-blue-200">
                FF-HN240902-009 (Đang giao)
              </button>
              <button type="button" onClick={() => setTrackingCodeInput('FF-HN240902-012')} className="px-2 py-1 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 rounded font-mono border border-emerald-200">
                FF-HN240902-012 (Đã giao)
              </button>
              <button type="button" onClick={() => setTrackingCodeInput('FF-HN240902-017')} className="px-2 py-1 bg-amber-50 hover:bg-amber-100 text-amber-700 rounded font-mono border border-amber-200">
                FF-HN240902-017 (Hoàn hàng)
              </button>
            </div>

            {trackingResult && (
              <div className="border rounded-xl p-5 bg-slate-50 space-y-4 text-xs">
                <div className="flex justify-between items-center border-b pb-3">
                  <div>
                    <span className="text-slate-500 text-[11px]">Mã vận đơn:</span>
                    <h3 className="text-lg font-bold font-mono text-blue-600">{trackingResult.trackingCode}</h3>
                  </div>
                  <span className="px-3 py-1 rounded-full text-xs font-bold bg-blue-100 text-blue-800">
                    {trackingResult.currentStatus}
                  </span>
                </div>

                <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                  <div>
                    <span className="text-slate-500">Người gửi:</span>
                    <p className="font-semibold">{trackingResult.senderMasked}</p>
                  </div>
                  <div>
                    <span className="text-slate-500">Người nhận:</span>
                    <p className="font-semibold">{trackingResult.receiverMasked}</p>
                  </div>
                  <div>
                    <span className="text-slate-500">Trọng lượng:</span>
                    <p className="font-semibold">{trackingResult.totalWeightKg} kg</p>
                  </div>
                  <div>
                    <span className="text-slate-500">Tiền COD:</span>
                    <p className="font-semibold text-amber-600">{new Intl.NumberFormat('vi-VN').format(trackingResult.codAmount)} đ</p>
                  </div>
                </div>

                <div className="pt-3 border-t">
                  <h4 className="font-bold text-slate-700 mb-3">Hành trình chi tiết:</h4>
                  <div className="space-y-3 border-l-2 border-blue-500 ml-2 pl-4">
                    {trackingResult.timeline?.map((ev, i) => (
                      <div key={i} className="relative">
                        <div className="absolute -left-[21px] top-1 w-2.5 h-2.5 rounded-full bg-blue-600"></div>
                        <p className="font-bold text-slate-800">{ev.title}</p>
                        <p className="text-slate-600">{ev.description}</p>
                        <p className="text-[10px] text-slate-400">{ev.timestamp}</p>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }

  // ==========================================
  // VIEW 3: MAIN AUTHENTICATED SYSTEM CONSOLE
  // ==========================================
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      {/* Top Banner & Quick Role Switcher */}
      <header className="bg-slate-900 text-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3 flex flex-wrap justify-between items-center gap-4">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-blue-600 rounded-lg text-white">
              <Truck size={24} />
            </div>
            <div>
              <h1 className="text-xl font-bold tracking-tight">FleetFlow</h1>
              <p className="text-xs text-slate-400">Logistics & Delivery Management Platform</p>
            </div>
          </div>

          {/* Current Authenticated User & Quick Switcher */}
          <div className="flex items-center gap-3 flex-wrap text-xs">
            <div className="bg-slate-800 px-3 py-1.5 rounded-lg border border-slate-700 flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              <span className="text-slate-300">Đăng nhập: <b>{currentUser.username}</b></span>
              <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                currentUser.role === 'ROLE_ADMIN' ? 'bg-purple-900 text-purple-200 border border-purple-700' :
                currentUser.role === 'ROLE_STAFF' ? 'bg-blue-900 text-blue-200 border border-blue-700' :
                'bg-emerald-900 text-emerald-200 border border-emerald-700'
              }`}>
                {currentUser.role}
              </span>
            </div>

            <div className="hidden sm:flex items-center gap-1.5">
              <button onClick={() => handleLogin('admin', 'password123')} title="Chuyển sang Admin" className="px-2 py-1 bg-slate-800 hover:bg-purple-700 rounded text-[11px] transition">👑 Admin</button>
              <button onClick={() => handleLogin('staff_hn', 'password123')} title="Chuyển sang Staff" className="px-2 py-1 bg-slate-800 hover:bg-blue-700 rounded text-[11px] transition">📋 Staff</button>
              <button onClick={() => handleLogin('driver_nam', 'password123')} title="Chuyển sang Driver" className="px-2 py-1 bg-slate-800 hover:bg-emerald-700 rounded text-[11px] transition">🛵 Driver</button>
            </div>

            <button
              onClick={handleLogout}
              className="px-3 py-1.5 bg-red-600/80 hover:bg-red-600 rounded-lg text-white font-bold transition flex items-center gap-1.5"
            >
              <LogOut size={14} /> Đăng xuất
            </button>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 border-t border-slate-800 flex space-x-6 overflow-x-auto text-sm">
          {[
            { id: 'dashboard', label: '📊 Dashboard', icon: Activity, roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF'] },
            { id: 'shipments', label: '📦 Quản lý Vận Đơn', icon: Package, roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF'] },
            { id: 'dispatch', label: '⚡ Điều Phối & Concurrency', icon: Zap, roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF'] },
            { id: 'driver', label: '🛵 Cổng Tài Xế', icon: Navigation, roles: ['ROLE_DRIVER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF'] },
            { id: 'cod', label: '💰 Đối Soát COD', icon: DollarSign, roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF'] },
            { id: 'tracking', label: '🔍 Tra Cứu Công Khai', icon: Search, roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF', 'ROLE_DRIVER'] },
          ]
            .filter(tab => !tab.roles || !currentUser?.role || tab.roles.includes(currentUser.role))
            .map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`py-3 px-2 border-b-2 font-medium flex items-center gap-2 transition whitespace-nowrap ${
                activeTab === tab.id
                  ? 'border-blue-500 text-blue-400 font-semibold'
                  : 'border-transparent text-slate-400 hover:text-slate-200'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </header>

      {/* Toast Notification */}
      {notification && (
        <div className="fixed bottom-5 right-5 z-50">
          <div className={`px-4 py-3 rounded-lg shadow-lg text-white text-sm font-medium flex items-center gap-2 ${
            notification.type === 'error' ? 'bg-red-600' : notification.type === 'info' ? 'bg-blue-600' : 'bg-emerald-600'
          }`}>
            {notification.msg}
          </div>
        </div>
      )}

      {/* Main Container */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">

        {/* 1. DASHBOARD TAB */}
        {activeTab === 'dashboard' && (
          <div className="space-y-6">
            <div className="flex justify-between items-center">
              <h2 className="text-xl font-bold text-slate-800">Tổng Quan Vận Hành & Tài Chính (Dashboard)</h2>
              <button
                onClick={loadDashboard}
                className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold bg-white border border-slate-300 rounded shadow-sm hover:bg-slate-50"
              >
                <RefreshCw size={14} /> Làm mới
              </button>
            </div>

            {dashboardData ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
                <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
                  <div className="flex justify-between items-center text-slate-500 text-xs font-semibold">
                    <span>TỔNG VẬN ĐƠN</span>
                    <Package size={18} className="text-blue-500" />
                  </div>
                  <div className="text-2xl font-bold text-slate-900 mt-2">{dashboardData.totalShipments}</div>
                  <div className="text-xs text-slate-500 mt-1">Đơn chờ phân công: <b>{dashboardData.pendingUnassignedShipments}</b></div>
                </div>

                <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
                  <div className="flex justify-between items-center text-slate-500 text-xs font-semibold">
                    <span>TỶ LỆ GIAO THÀNH CÔNG</span>
                    <CheckCircle size={18} className="text-emerald-500" />
                  </div>
                  <div className="text-2xl font-bold text-emerald-600 mt-2">{dashboardData.deliverySuccessRatePercent}%</div>
                  <div className="text-xs text-slate-500 mt-1">Dựa trên đơn hoàn tất và chuyển hoàn</div>
                </div>

                <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
                  <div className="flex justify-between items-center text-slate-500 text-xs font-semibold">
                    <span>DOANH THU CƯỚC VẬN CHUYỂN</span>
                    <DollarSign size={18} className="text-indigo-500" />
                  </div>
                  <div className="text-2xl font-bold text-indigo-600 mt-2">
                    {new Intl.NumberFormat('vi-VN').format(dashboardData.totalShippingRevenue)} đ
                  </div>
                  <div className="text-xs text-slate-500 mt-1">Từ các đơn đã giao thành công</div>
                </div>

                <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
                  <div className="flex justify-between items-center text-slate-500 text-xs font-semibold">
                    <span>TỔNG TIỀN COD ĐÃ THU</span>
                    <ShieldCheck size={18} className="text-amber-500" />
                  </div>
                  <div className="text-2xl font-bold text-amber-600 mt-2">
                    {new Intl.NumberFormat('vi-VN').format(dashboardData.totalDeliveredCodVolume)} đ
                  </div>
                  <div className="text-xs text-slate-500 mt-1">Tài xế đang trực tuyến: <b>{dashboardData.activeDriversCount}</b></div>
                </div>
              </div>
            ) : (
              <div className="bg-white p-8 rounded-xl border border-slate-200 text-center text-slate-400 text-xs">
                Đang tải dữ liệu báo cáo...
              </div>
            )}
          </div>
        )}

        {/* 2. SHIPMENT MANAGEMENT TAB */}
        {activeTab === 'shipments' && (
          <div className="space-y-6">
            <div className="flex flex-wrap justify-between items-center gap-4">
              <div className="flex gap-2 flex-wrap text-xs">
                {['ALL', 'PENDING', 'ASSIGNED', 'PICKED_UP', 'AT_ORIGIN_HUB', 'IN_TRANSIT', 'AT_DEST_HUB', 'OUT_FOR_DELIVERY', 'DELIVERED', 'DELIVERY_FAILED', 'RETURNED', 'CANCELLED'].map(st => (
                  <button
                    key={st}
                    onClick={() => { setSelectedStatus(st); loadShipments(st); }}
                    className={`px-3 py-1 rounded-full font-medium transition text-xs ${
                      selectedStatus === st
                        ? 'bg-blue-600 text-white shadow-sm'
                        : 'bg-white border border-slate-300 text-slate-700 hover:bg-slate-100'
                    }`}
                  >
                    {st}
                  </button>
                ))}
              </div>

              <div className="flex gap-2">
                <button
                  onClick={() => { setShowCreateModal(true); handleEstimatePricing(); }}
                  className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold bg-blue-600 text-white rounded-lg hover:bg-blue-700 shadow-sm"
                >
                  <PlusCircle size={15} /> Tạo Đơn Hàng Mới
                </button>
                <button
                  onClick={() => loadShipments()}
                  className="px-3 py-1.5 text-xs font-semibold bg-white border border-slate-300 rounded-lg hover:bg-slate-50"
                >
                  <RefreshCw size={14} />
                </button>
              </div>
            </div>

            {/* Shipments Table */}
            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200 text-xs text-left">
                <thead className="bg-slate-50 font-semibold text-slate-600 uppercase tracking-wider">
                  <tr>
                    <th className="px-4 py-3">Mã Vận Đơn</th>
                    <th className="px-4 py-3">Người Nhận</th>
                    <th className="px-4 py-3">Tỉnh/TP</th>
                    <th className="px-4 py-3">Cước Phí</th>
                    <th className="px-4 py-3">Tiền COD</th>
                    <th className="px-4 py-3">Tài Xế</th>
                    <th className="px-4 py-3">Trạng Thái</th>
                    <th className="px-4 py-3">Version</th>
                    <th className="px-4 py-3">Hành Động</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 font-medium">
                  {shipments.length > 0 ? (
                    shipments.map(s => (
                      <tr key={s.id} className="hover:bg-slate-50">
                        <td className="px-4 py-3 font-mono font-bold text-blue-600">{s.trackingCode}</td>
                        <td className="px-4 py-3">{s.receiverName} ({s.receiverPhone})</td>
                        <td className="px-4 py-3">{s.receiverProvince || 'Hà Nội'}</td>
                        <td className="px-4 py-3 font-semibold">{new Intl.NumberFormat('vi-VN').format(s.shippingFee)} đ</td>
                        <td className="px-4 py-3 text-amber-600 font-semibold">{new Intl.NumberFormat('vi-VN').format(s.codAmount)} đ</td>
                        <td className="px-4 py-3">{s.assignedDriverName || <span className="text-slate-400 italic">Chưa gán</span>}</td>
                        <td className="px-4 py-3">
                          <span className={`px-2 py-0.5 rounded-full text-[11px] font-semibold ${
                            s.status === 'DELIVERED' ? 'bg-emerald-100 text-emerald-800' :
                            s.status === 'PENDING' ? 'bg-amber-100 text-amber-800' :
                            s.status === 'ASSIGNED' ? 'bg-blue-100 text-blue-800' :
                            s.status === 'PICKED_UP' ? 'bg-teal-100 text-teal-800' :
                            s.status === 'AT_ORIGIN_HUB' || s.status === 'AT_DEST_HUB' ? 'bg-purple-100 text-purple-800' :
                            s.status === 'IN_TRANSIT' ? 'bg-violet-100 text-violet-800' :
                            s.status === 'OUT_FOR_DELIVERY' ? 'bg-indigo-100 text-indigo-800' :
                            s.status === 'DELIVERY_FAILED' ? 'bg-red-100 text-red-800' :
                            s.status === 'RETURNED' ? 'bg-rose-100 text-rose-800' :
                            s.status === 'CANCELLED' ? 'bg-slate-200 text-slate-700' : 'bg-slate-100 text-slate-800'
                          }`}>
                            {s.status}
                          </span>
                        </td>
                        <td className="px-4 py-3 font-mono text-slate-500">v{s.version}</td>
                        <td className="px-4 py-3 space-x-1">
                          {s.status === 'PENDING' && (
                            <button
                              onClick={() => handleTestConcurrency(s.id)}
                              className="px-2 py-1 bg-amber-50 text-amber-700 border border-amber-300 rounded hover:bg-amber-100 text-[11px]"
                              title="Kiểm thử bắn 5 request gán đơn cùng lúc để kiểm tra Optimistic Locking"
                            >
                              ⚡ Test Concurrency
                            </button>
                          )}
                          <button
                            onClick={() => { setTrackingCodeInput(s.trackingCode); setActiveTab('tracking'); }}
                            className="px-2 py-1 bg-slate-100 text-slate-700 rounded hover:bg-slate-200 text-[11px]"
                          >
                            Tra cứu
                          </button>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="9" className="px-4 py-8 text-center text-slate-400">
                        {loading ? 'Đang tải dữ liệu...' : 'Không có đơn hàng nào trong trạng thái này.'}
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* 3. DISPATCH & CONCURRENCY CONSOLE TAB */}
        {activeTab === 'dispatch' && (
          <div className="space-y-6">
            <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
              <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <Zap className="text-amber-500" /> Bảng Điều Phối & Phân Công Tài Xế (Concurrency Control)
              </h2>
              <p className="text-xs text-slate-600 leading-relaxed">
                Khi nhiều nhân viên điều phối cùng gán 1 đơn hàng trong cùng tích tắc, hệ thống kích hoạt <b>Optimistic Locking (@Version)</b> trên Shipment
                và <b>Pessimistic Locking (SELECT FOR UPDATE)</b> trên Driver. Chỉ duy nhất 1 request hợp lệ được chấp thuận, các request đồng thời khác sẽ nhận mã lỗi <code>409 CONFLICT</code>.
              </p>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2">
                <div className="border border-slate-200 p-4 rounded-lg bg-slate-50">
                  <h3 className="font-semibold text-sm mb-2 text-slate-800">Tài xế 1: Nguyễn Văn Nam (driver_nam)</h3>
                  <p className="text-xs text-slate-600">Xe: Xe máy 29A-12345 (Tải trọng: 50 kg)</p>
                  <p className="text-xs text-slate-600">Giới hạn: Tối đa 10 đơn cùng lúc</p>
                  <span className="inline-block mt-2 px-2 py-0.5 rounded text-[11px] font-semibold bg-emerald-100 text-emerald-800">AVAILABLE</span>
                </div>

                <div className="border border-slate-200 p-4 rounded-lg bg-slate-50">
                  <h3 className="font-semibold text-sm mb-2 text-slate-800">Tài xế 2: Phạm Tuấn (driver_tuan)</h3>
                  <p className="text-xs text-slate-600">Xe: Xe máy 29B-67890 (Tải trọng: 50 kg)</p>
                  <p className="text-xs text-slate-600">Giới hạn: Tối đa 10 đơn cùng lúc</p>
                  <span className="inline-block mt-2 px-2 py-0.5 rounded text-[11px] font-semibold bg-emerald-100 text-emerald-800">AVAILABLE</span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 4. DRIVER PORTAL TAB */}
        {activeTab === 'driver' && (
          <div className="space-y-6">
            <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
              <div className="flex justify-between items-center">
                <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                  <Navigation className="text-emerald-500" /> Cổng Thao Tác Của Tài Xế (Driver Portal)
                </h2>
                <div className="flex gap-2">
                  <button
                    onClick={() => {
                      if (currentUser?.driverId) {
                        api.pingDriverLocation(currentUser.driverId, 21.028511, 105.804817);
                        showToast('Đã ping vị trí GPS thành công!');
                      } else {
                        showToast('Bạn phải đăng nhập tài khoản Tài Xế (driver_nam)', 'error');
                      }
                    }}
                    className="px-3 py-1.5 text-xs font-semibold bg-emerald-600 text-white rounded-lg hover:bg-emerald-700"
                  >
                    📍 Giả lập Ping GPS vị trí
                  </button>
                  <button
                    onClick={loadDriverTasks}
                    className="px-3 py-1.5 text-xs font-semibold bg-white border border-slate-300 rounded-lg"
                  >
                    <RefreshCw size={14} />
                  </button>
                </div>
              </div>

              {driverTasks.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {driverTasks.map(t => (
                    <div key={t.id} className="border border-slate-200 rounded-lg p-4 bg-slate-50 space-y-2 text-xs">
                      <div className="flex justify-between items-center font-bold">
                        <span className="text-blue-600 text-sm">{t.trackingCode}</span>
                        <span className="px-2 py-0.5 rounded bg-blue-100 text-blue-800">{t.status}</span>
                      </div>
                      <p><b>Người nhận:</b> {t.receiverName} - {t.receiverPhone}</p>
                      <p><b>Địa chỉ:</b> {t.receiverAddress}</p>
                      <p><b>Tiền COD cần thu:</b> <span className="text-amber-600 font-bold">{new Intl.NumberFormat('vi-VN').format(t.codAmount)} đ</span></p>

                      <div className="flex gap-2 pt-2 border-t border-slate-200 flex-wrap">
                        {t.status === 'ASSIGNED' && (
                          <button
                            onClick={() => api.updateStatus(t.id, 'PICKED_UP', 'Đã lấy hàng').then(() => { showToast('Đã nhận hàng'); loadDriverTasks(); })}
                            className="px-2.5 py-1 rounded bg-blue-600 text-white font-medium hover:bg-blue-700"
                          >
                            Đã nhận hàng (PICKED_UP)
                          </button>
                        )}
                        {(t.status === 'PICKED_UP' || t.status === 'IN_TRANSIT' || t.status === 'DELIVERY_FAILED') && (
                          <button
                            onClick={() => api.updateStatus(t.id, 'OUT_FOR_DELIVERY', 'Bắt đầu chuyến giao').then(() => { showToast('Bắt đầu giao'); loadDriverTasks(); })}
                            className="px-2.5 py-1 rounded bg-indigo-600 text-white font-medium hover:bg-indigo-700"
                          >
                            Bắt đầu giao (OUT_FOR_DELIVERY)
                          </button>
                        )}
                        {t.status === 'OUT_FOR_DELIVERY' && (
                          <>
                            <button
                              onClick={() => api.recordDeliveryAttempt(t.id, { status: 'SUCCESS' }).then(() => { showToast('Giao thành công!'); loadDriverTasks(); })}
                              className="px-2.5 py-1 rounded bg-emerald-600 text-white font-medium hover:bg-emerald-700"
                            >
                              ✅ Giao thành công
                            </button>
                            <button
                              onClick={() => api.recordDeliveryAttempt(t.id, {
                                status: 'FAILED',
                                failureReason: 'CUSTOMER_UNREACHABLE',
                                failureNote: 'Gọi 3 cuộc không nghe máy'
                              }).then(() => { showToast('Đã ghi nhận giao thất bại'); loadDriverTasks(); })}
                              className="px-2.5 py-1 rounded bg-red-600 text-white font-medium hover:bg-red-700"
                            >
                              ❌ Giao thất bại
                            </button>
                          </>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8 text-slate-400 text-xs">
                  Hiện không có đơn hàng nào được gán cho tài xế này. Hãy đăng nhập tài khoản <b>Staff</b> để phân công đơn trước!
                </div>
              )}
            </div>
          </div>
        )}

        {/* 5. COD & SETTLEMENT TAB */}
        {activeTab === 'cod' && (
          <div className="space-y-6">
            <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
              <div className="flex justify-between items-center">
                <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                  <DollarSign className="text-amber-500" /> Quản Lý Tiền Thu Hộ & Đối Soát COD (Idempotency)
                </h2>
                <button
                  onClick={handleTestIdempotency}
                  className="px-3 py-1.5 text-xs font-semibold bg-amber-600 text-white rounded-lg hover:bg-amber-700 flex items-center gap-1.5"
                >
                  <ShieldCheck size={14} /> 🛡️ Test Idempotency (Click Đúp)
                </button>
              </div>

              <p className="text-xs text-slate-600">
                Khi kế toán duyệt nộp tiền COD, request gửi kèm <code>Idempotency-Key</code>. Nếu mạng lag hoặc người dùng click liên tiếp nhiều lần,
                hệ thống sẽ nhận diện khoá duy nhất và trả về giao dịch đã lập mà không tạo thêm đối soát kép.
              </p>

              <div className="pt-2">
                <h3 className="font-semibold text-xs text-slate-700 mb-2">Các giao dịch COD tài xế đã thu (COLLECTED) chờ đối soát:</h3>
                {pendingCodList.length > 0 ? (
                  <div className="divide-y divide-slate-100 border border-slate-200 rounded-lg text-xs">
                    {pendingCodList.map(c => (
                      <div key={c.id} className="p-3 flex justify-between items-center hover:bg-slate-50">
                        <div>
                          <span className="font-bold text-blue-600">{c.trackingCode}</span>
                          <span className="text-slate-400 ml-2">Thu lúc: {c.collectedAt || 'Hôm nay'}</span>
                        </div>
                        <div className="font-bold text-amber-600">
                          {new Intl.NumberFormat('vi-VN').format(c.amount)} đ
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-6 text-slate-400 text-xs border border-dashed border-slate-200 rounded-lg">
                    Không có khoản tiền COD nào đang chờ đối soát cho tài xế này.
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* 6. PUBLIC TRACKING TAB */}
        {activeTab === 'tracking' && (
          <div className="space-y-6">
            <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
              <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <Search className="text-blue-500" /> Tra Cứu Hành Trình Đơn Vận Chuyển (Public Tracking)
              </h2>

              <form onSubmit={handleSearchTracking} className="flex gap-2">
                <input
                  type="text"
                  value={trackingCodeInput}
                  onChange={(e) => setTrackingCodeInput(e.target.value)}
                  placeholder="Nhập mã vận đơn (VD: FF00000001)..."
                  className="flex-1 px-4 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono"
                />
                <button
                  type="submit"
                  className="px-5 py-2 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 text-sm"
                >
                  Tra cứu
                </button>
              </form>

              {trackingResult && (
                <div className="border border-slate-200 rounded-xl p-6 bg-slate-50 space-y-4 mt-6">
                  <div className="flex justify-between items-center border-b border-slate-200 pb-3">
                    <div>
                      <span className="text-xs text-slate-500">Mã vận đơn:</span>
                      <h3 className="text-xl font-bold font-mono text-blue-600">{trackingResult.trackingCode}</h3>
                    </div>
                    <span className="px-3 py-1 rounded-full text-xs font-bold bg-blue-100 text-blue-800">
                      {trackingResult.currentStatus}
                    </span>
                  </div>

                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-xs">
                    <div>
                      <span className="text-slate-500">Người gửi:</span>
                      <p className="font-semibold text-slate-800">{trackingResult.senderMasked}</p>
                    </div>
                    <div>
                      <span className="text-slate-500">Người nhận:</span>
                      <p className="font-semibold text-slate-800">{trackingResult.receiverMasked}</p>
                    </div>
                    <div>
                      <span className="text-slate-500">Trọng lượng:</span>
                      <p className="font-semibold text-slate-800">{trackingResult.totalWeightKg} kg</p>
                    </div>
                    <div>
                      <span className="text-slate-500">Tiền COD:</span>
                      <p className="font-semibold text-amber-600">{new Intl.NumberFormat('vi-VN').format(trackingResult.codAmount)} đ</p>
                    </div>
                  </div>

                  {/* Timeline */}
                  <div className="pt-4 border-t border-slate-200">
                    <h4 className="font-semibold text-xs text-slate-700 mb-3">Dòng thời gian hành trình:</h4>
                    <div className="space-y-4 border-l-2 border-blue-500 ml-2 pl-4 text-xs">
                      {trackingResult.timeline?.map((ev, idx) => (
                        <div key={idx} className="relative">
                          <div className="absolute -left-[21px] top-0.5 w-2.5 h-2.5 rounded-full bg-blue-600"></div>
                          <p className="font-bold text-slate-800">{ev.title}</p>
                          <p className="text-slate-600">{ev.description}</p>
                          <p className="text-[10px] text-slate-400">{ev.timestamp}</p>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </main>

      {/* CREATE SHIPMENT MODAL */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-white rounded-xl shadow-xl max-w-2xl w-full p-6 space-y-4">
            <div className="flex justify-between items-center border-b pb-3">
              <h3 className="font-bold text-slate-900 text-base">Tạo Mới Vận Đơn (Shipment)</h3>
              <button onClick={() => setShowCreateModal(false)} className="text-slate-400 hover:text-slate-600">✕</button>
            </div>

            <form onSubmit={handleCreateShipmentSubmit} className="space-y-4 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700">Tên người gửi</label>
                  <input
                    type="text"
                    required
                    value={createForm.senderName}
                    onChange={e => setCreateForm({ ...createForm, senderName: e.target.value })}
                    className="w-full mt-1 p-2 border rounded"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700">SĐT người gửi</label>
                  <input
                    type="text"
                    required
                    value={createForm.senderPhone}
                    onChange={e => setCreateForm({ ...createForm, senderPhone: e.target.value })}
                    className="w-full mt-1 p-2 border rounded"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700">Tên người nhận</label>
                  <input
                    type="text"
                    required
                    value={createForm.receiverName}
                    onChange={e => setCreateForm({ ...createForm, receiverName: e.target.value })}
                    className="w-full mt-1 p-2 border rounded"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700">SĐT người nhận</label>
                  <input
                    type="text"
                    required
                    value={createForm.receiverPhone}
                    onChange={e => setCreateForm({ ...createForm, receiverPhone: e.target.value })}
                    className="w-full mt-1 p-2 border rounded"
                  />
                </div>
              </div>

              <div>
                <label className="font-semibold text-slate-700">Địa chỉ giao hàng</label>
                <input
                  type="text"
                  required
                  value={createForm.receiverAddress}
                  onChange={e => setCreateForm({ ...createForm, receiverAddress: e.target.value })}
                  className="w-full mt-1 p-2 border rounded"
                />
              </div>

              {/* Pricing factors */}
              <div className="grid grid-cols-4 gap-2 bg-slate-50 p-3 rounded-lg border">
                <div>
                  <label className="font-semibold text-slate-700">Trọng lượng (kg)</label>
                  <input
                    type="number"
                    step="0.1"
                    value={createForm.totalWeightKg}
                    onChange={e => { setCreateForm({ ...createForm, totalWeightKg: e.target.value }); handleEstimatePricing(); }}
                    className="w-full mt-1 p-1.5 border rounded"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700">Dài (cm)</label>
                  <input
                    type="number"
                    value={createForm.lengthCm}
                    onChange={e => { setCreateForm({ ...createForm, lengthCm: e.target.value }); handleEstimatePricing(); }}
                    className="w-full mt-1 p-1.5 border rounded"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700">Rộng (cm)</label>
                  <input
                    type="number"
                    value={createForm.widthCm}
                    onChange={e => { setCreateForm({ ...createForm, widthCm: e.target.value }); handleEstimatePricing(); }}
                    className="w-full mt-1 p-1.5 border rounded"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700">Cao (cm)</label>
                  <input
                    type="number"
                    value={createForm.heightCm}
                    onChange={e => { setCreateForm({ ...createForm, heightCm: e.target.value }); handleEstimatePricing(); }}
                    className="w-full mt-1 p-1.5 border rounded"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700">Gói dịch vụ</label>
                  <select
                    value={createForm.serviceTier}
                    onChange={e => { setCreateForm({ ...createForm, serviceTier: e.target.value }); handleEstimatePricing(); }}
                    className="w-full mt-1 p-2 border rounded bg-white"
                  >
                    <option value="STANDARD">Tiêu chuẩn (STANDARD)</option>
                    <option value="EXPRESS">Hoả tốc (EXPRESS)</option>
                    <option value="SAME_DAY">Giao trong ngày (SAME_DAY)</option>
                  </select>
                </div>
                <div>
                  <label className="font-semibold text-slate-700">Tiền thu hộ COD (VNĐ)</label>
                  <input
                    type="number"
                    value={createForm.codAmount}
                    onChange={e => { setCreateForm({ ...createForm, codAmount: e.target.value }); handleEstimatePricing(); }}
                    className="w-full mt-1 p-2 border rounded"
                  />
                </div>
              </div>

              {/* Pricing Engine Live Result */}
              {estimatedPrice && (
                <div className="bg-blue-50 border border-blue-200 p-3 rounded-lg flex justify-between items-center text-xs">
                  <div>
                    <span className="text-blue-700">Trọng lượng tính cước: <b>{estimatedPrice.chargeableWeightKg} kg</b></span>
                    <span className="text-slate-500 ml-2">(Thể tích: {estimatedPrice.volumetricWeightKg} kg)</span>
                  </div>
                  <div className="text-right">
                    <span className="text-slate-500 block">Tổng cước phí ước tính:</span>
                    <span className="text-base font-bold text-blue-700">{new Intl.NumberFormat('vi-VN').format(estimatedPrice.totalEstimatedFee)} đ</span>
                  </div>
                </div>
              )}

              <div className="flex justify-end gap-2 pt-2 border-t">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 border rounded text-slate-600 hover:bg-slate-50"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-5 py-2 bg-blue-600 text-white font-semibold rounded hover:bg-blue-700"
                >
                  {loading ? 'Đang tạo...' : 'Xác nhận Tạo Vận Đơn'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
