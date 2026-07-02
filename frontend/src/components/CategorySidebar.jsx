export default function CategorySidebar({ categories, selected, onSelect }) {
  return (
    <aside className="category-sidebar">
      <h3>Categorías</h3>
      <ul>
        <li>
          <button
            className={!selected ? 'active' : ''}
            onClick={() => onSelect(null)}
          >
            Todas
          </button>
        </li>
        {categories.map((cat) => (
          <li key={cat.id}>
            <button
              className={selected === cat.id ? 'active' : ''}
              onClick={() => onSelect(cat.id)}
            >
              {cat.name}
            </button>
          </li>
        ))}
      </ul>
    </aside>
  )
}
