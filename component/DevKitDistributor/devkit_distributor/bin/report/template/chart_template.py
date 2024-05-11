CHART_TEMPLATE = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CSV Line Charts</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100vh;
            margin: 0;
        }
        p.devkit_name {
            margin-left: 70px;
            top: auto;
        }
        #icon {
            margin-left: 100px;
        }
        #sidePanel {
            height: 100%;
            width: 250px;
            position: fixed;
            z-index: 1;
            top: 0;
            left: 0;
            background-color: #111;
            padding-top: 20px;
            color: white;
        }
        .chartContainer {
            display: flex;
            flex-direction: column;
            align-items: center;
            margin: 20px;
            position: relative;
            top: 100px;
        }
        canvas {
            max-width: 100%;
            max-height: 100%;
        }
        .statistics {
            position: absolute;
            top: 10px;
            right : -100px;
            text-align: right;
        }
        #goBackBtn {
            background-color: #831020;
            color: white;
            padding: 10px;
            text-align: center;
            text-decoration: none;
            display: block;
            font-size: 16px;
            margin-top: 10px;
            cursor: pointer;
            width: 100%;
            border: none;
        }
    </style>
</head>
<body>
<div id="sidePanel">
    <img id="icon" src="kunpeng_devkit.png" alt="Icon" width="50">
    <p class="devkit_name"> Kunpeng DevKit </p>
    <button id="goBackBtn" onclick="goBack()">Go Back</button>
</div>

<div class="chartContainer">
    <h2>Elapsed Time</h2>
    <canvas id="elapsedChart" width="400" height="200"></canvas>
    <div class="statistics" id="elapsedStatistics">
        <p>Mean: <span id="elapsedMean"></span></p>
        <p>Max: <span id="elapsedMax"></span></p>
        <p>Min: <span id="elapsedMin"></span></p>
    </div>
</div>

<div class="chartContainer">
    <h2>Idle Time</h2>
    <canvas id="idleChart" width="400" height="200"></canvas>
    <div class="statistics" id="idleStatistics">
        <p>Mean: <span id="idleMean"></span></p>
        <p>Max: <span id="idleMax"></span></p>
        <p>Min: <span id="idleMin"></span></p>
    </div>
</div>

<div class="chartContainer">
    <h2>Latency</h2>
    <canvas id="latencyChart" width="400" height="200"></canvas>
    <div class="statistics" id="latencyStatistics">
        <p>Mean: <span id="latencyMean"></span></p>
        <p>Max: <span id="latencyMax"></span></p>
        <p>Min: <span id="latencyMin"></span></p>
    </div>
</div>

<script src="jquery-3.6.4.js"></script>
<script>
    const csvData = `
    {{csv_content}}
    `;
    processData(csvData);
    function processData(csvData) {
        const lines = csvData.trim().split('\\n');
        const labels = [];
        const data = {
            'elapsed': {},
            'idle': {},
            'latency': {}
        };
        
        for (let i = 1; i < lines.length; i++) {
            const values = lines[i].split(',');
            const time = values[0];
            const elapsed = parseFloat(values[1]);
            const idle = parseFloat(values[2]);
            const latency = parseFloat(values[3]);
            const label = values[4];
            
            if (!labels.includes(time)) {
                labels.push(time);
            }
            
            for (const category of ['elapsed', 'idle', 'latency']) {
                if (!data[category][label]) {
                    data[category][label] = [];
                }
                data[category][label].push(category === 'elapsed' ? elapsed : category === 'idle' ? idle : latency);
            }
        }
        createLineChart('elapsedChart', labels, data['elapsed'], 'elapsed');
        createLineChart('idleChart', labels, data['idle'], 'idle');
        createLineChart('latencyChart', labels, data['latency'], 'latency');
        
    }
        
    function createLineChart(chartId, labels, data, category) {
        const ctx = document.getElementById(chartId).getContext('2d');
        
        const datasets = Object.keys(data).map((label, index) => ({
            label: label,
            data: data[label],
            borderColor: getRandomColor(),
            fill: false,
        }));
        
        const chartData = {
            labels: labels,
            datasets: datasets,
        };
        
        new Chart(ctx, {
            type: 'line',
            data: chartData,
        });
        
        displayStatistics(data, category);
        
    }
    
    function displayStatistics(data, category) {
        const meanElement = document.getElementById(`${category}Mean`);
        const maxElement = document.getElementById(`${category}Max`);
        const minElement = document.getElementById(`${category}Min`);
        
        for (const label in data) {
            const values = data[label];
            const mean = calculateMean(values);
            const max = Math.max(...values);
            const min = Math.min(...values);
            meanElement.innerHTML = mean.toFixed(2);
            maxElement.innerHTML = max.toFixed(2);
            minElement.innerHTML = min.toFixed(2);
        }
    }

    function calculateMean(values) {
        const sum = values.reduce((acc, value) => acc + value, 0);
        return sum / values.length;
    }
    
    function getRandomColor() {
        const letters = '0123456789ABCDEF';
        let color = '#';
        for (let i = 0; i < 6; i++) {
            color += letters[Math.floor(Math.random() * 16)];
        }
        return color;
    }
    
    function goBack() {
        window.location.href = "performance_report.html";
    }    
</script>
</body>
</html>
"""