import CheckItem from './CheckItem';

function CheckList({ checks, onCheckClick }) {
  if (!checks || checks.length === 0) {
    return (
      <div style={{ color: '#6b7280', fontSize: '14px' }}>
        Нет проверок. Нажмите «Добавить новую проверку».
      </div>
    );
  }

  return (
    <div style={{ display: 'grid', gap: '10px', overflow: 'hidden' }}>
      {checks.map((check) => (
        <CheckItem key={check.id} check={check} onClick={() => onCheckClick && onCheckClick(check)} />
      ))}
    </div>
  );
}

export default CheckList;


