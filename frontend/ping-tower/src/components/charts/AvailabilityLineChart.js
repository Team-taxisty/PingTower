import EChart from '../EChart';

function AvailabilityLineChart({ points }) {
  const option = {
    backgroundColor: 'transparent',
    animation: true,
    grid: { left: 30, right: 12, top: 20, bottom: 24 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: points.map((p) => p.t),
      axisLine: { lineStyle: { color: '#374151' } },
      axisLabel: { color: '#000' },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: 'rgba(156,163,175,0.15)' } },
      axisLabel: { color: '#9ca3af', formatter: '{value}%' }
    },
    tooltip: { trigger: 'axis' },
    series: [
      {
        type: 'line',
        data: points.map((p) => p.v),
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        lineStyle: { width: 2, color: '#10b981' },
        areaStyle: { color: 'rgba(16,185,129,0.08)' }
      }
    ]
  };
  return <EChart option={option} />;
}

export default AvailabilityLineChart;


