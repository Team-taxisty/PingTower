import { useEffect, useRef } from 'react';
import * as echarts from 'echarts';

function EChart({ option, style }) {
  const ref = useRef(null);
  const chartRef = useRef(null);

  useEffect(() => {
    if (!ref.current) return;
    chartRef.current = echarts.init(ref.current, undefined, { renderer: 'canvas' });
    function handleResize() {
      chartRef.current && chartRef.current.resize();
    }
    window.addEventListener('resize', handleResize);
    return () => {
      window.removeEventListener('resize', handleResize);
      chartRef.current && chartRef.current.dispose();
    };
  }, []);

  useEffect(() => {
    if (!chartRef.current) return;
    chartRef.current.setOption(option, true);
  }, [option]);

  const containerStyle = {
    width: '100%',
    minHeight: 220,
    maxHeight: 300,
    overflow: 'hidden',
    ...style
  };

  return <div ref={ref} style={containerStyle} />;
}

export default EChart;


