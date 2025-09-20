import EChart from '../EChart';

function StatusPieChart({ data }) {
  const option = {
    backgroundColor: 'transparent',
    tooltip: { 
      trigger: 'item',
      backgroundColor: '#ffffff',
      borderColor: '#E5B8E8',
      textStyle: { color: '#1a1a1a' }
    },
    legend: { show: false },
    series: [
      {
        name: 'Статусы',
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 8},
        label: { 
          color: '#6D0475',
          fontSize: 12,
          fontWeight: 500
        },
        labelLine: {
          show: true,
          length: 10,
          length2: 5
        },
        data: [
          { value: data.ok, name: 'ОК', itemStyle: { color: '#10b981' } },
          { value: data.degraded, name: 'Деградация', itemStyle: { color: '#f59e0b' } },
          { value: data.down, name: 'Недоступен', itemStyle: { color: '#ef4444' } }
        ]
      }
    ]
  };
  return <EChart option={option} />;
}

export default StatusPieChart;


