import { Navigate, Route, Routes } from 'react-router'
import { RequireRole } from './auth/RequireRole'
import { Layout } from './components/Layout'
import { LoginPage } from './pages/LoginPage'
import { CatalogPage } from './pages/client/CatalogPage'
import { MerchantsPage } from './pages/client/MerchantsPage'
import { MyOrdersPage } from './pages/client/MyOrdersPage'
import { OrderDetailPage } from './pages/client/OrderDetailPage'
import { MerchantOrdersPage } from './pages/merchant/MerchantOrdersPage'
import { MerchantProductsPage } from './pages/merchant/MerchantProductsPage'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/comercios" element={<RequireRole role="CLIENT"><MerchantsPage /></RequireRole>} />
        <Route path="/comercios/:merchantId" element={<RequireRole role="CLIENT"><CatalogPage /></RequireRole>} />
        <Route path="/mis-pedidos" element={<RequireRole role="CLIENT"><MyOrdersPage /></RequireRole>} />
        <Route path="/mis-pedidos/:orderId" element={<RequireRole role="CLIENT"><OrderDetailPage /></RequireRole>} />
        <Route path="/comercio/pedidos" element={<RequireRole role="MERCHANT"><MerchantOrdersPage /></RequireRole>} />
        <Route path="/comercio/productos" element={<RequireRole role="MERCHANT"><MerchantProductsPage /></RequireRole>} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Route>
    </Routes>
  )
}
