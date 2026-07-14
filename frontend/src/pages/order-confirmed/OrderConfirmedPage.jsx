import ConfirmedOrder from './components/ConfirmedOrder'

export default function OrderConfirmedPage({ order, onBackToCatalog }) {
  return (
    <div className="store-body">
      <main className="store-main">
        <ConfirmedOrder order={order} onBackToCatalog={onBackToCatalog} />
      </main>
    </div>
  )
}
