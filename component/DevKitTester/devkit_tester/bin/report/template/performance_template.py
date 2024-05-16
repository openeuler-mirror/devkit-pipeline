PERFORMANCE_TEMPLATE = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Main Page</title>
    <style>
        p.devkit_name {
            margin-left: 70px;
            top: auto;
        }
        body {
            margin: 0;
            font-family: Arial, sans-serif;
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
        #content {
            margin-left: 250px;
            padding: 20px;
        }
        .dropdown {
            display: inline-block;
            width: 100%;
        }
        .dropdown-content {
            display: none;
            position: absolute;
            background-color: transparent;
            min-width: 100%;
            box-shadow: 0 8px 16px 0 rgba(0,0,0,0.2);
            z-index: 1;
            color: white;
        }
        .dropdown-content a {
            color: white;
            padding: 12px 16px;
            text-decoration: none;
            display: block;
            text-align: center;
            border-bottom: 1px solid #333;
        }
        .dropdown-content a:last-child {
            border-bottom: none;
        }
        .dropdown-content a:hover {
            background-color: #831020;
            color: white;
        }
        .dropdown.open .dropdown-content {
            display: block;
        }
        .dropbtn {
            background-color: #831020;
            color: white;
            padding: 10px;
            text-align: center;
            text-decoration: none;
            display: block;
            font-size: 16px;
            margin-bottom: 10px;
            cursor: pointer;
            width: 100%;
        }
        table {
            border-collapse: collapse;
            width: 80%;
            margin: 20px 0;
        }
        th, td {
            border: 1px solid #dddddd;
            padding: 8px;
        }
        th, td:first-child {
            text-align: left;
        }
        th {
            background-color: #f2f2f2;
        }
        td:first-child {
            background-color: #808080;
            color: white;
        }
    </style>
</head>
<body>
<div id="sidePanel">
    <img id="icon" src="kunpeng_devkit.png" alt="Icon" width="50">
    <p class="devkit_name"> Kunpeng DevKit </p>
    <div class="dropdown">
        <button class="dropbtn" onclick="toggleDropdown()">xingneng lie hua men jin baogao </button>
        <div class="dropdown-content">
            <a href="#" onclick="openTable()">xingneng qushi</a>
            <a href="#" onclick="openGitRecord()">git log</a>
            <a href="#" onclick="openPerformanceChart()">xingneng qushi tu</a>
        </div>
    </div>
</div>

<div id="content">
    <h1>DevKit Performance Test Info</h1>
    
    <table id="mainJsonTable" class="display">
        <tbody>
        </tbody>
    </table>
</div>

<script src="jquery-3.6.4.js"></script>

<script>
    var mainJsonData = {{info_dict}};
    
    var mainTableRows = Object.entries(mainJsonData).map(([key, value]) => {
        return "<tr><td>" + key + "</td><td>" + value + "</td></tr>";
    }).join("");
    
    $('#mainJsonTable tbody').html(mainTableRows);
    
    $('#mainJsonTable').DataTable({
        searching: false,
        paging: false,
        info: false
    });
    
    function openTable() {
        window.location.href = "performance_summary.html";
    }
    
    function openGitRecord() {
        window.location.href = "git_record.html";
    }

    function openPerformanceChart() {
        window.location.href = "performance_chart.html";
    }
    
    function toggleDropdown() {
        var dropdown = document.querySelector('.dropdown');
        dropdown.classList.toggle('open');
    }
</script>

</body>
</html>    
"""
