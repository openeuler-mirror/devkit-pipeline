lGIT_LOG_TEMPLATE = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Table Page</title>
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
        #content {
            margin-left: 250px;
            padding: 20px;
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
    <button id="goBackBtn" onclick="goBack()">Go Back</button>
</div>

<div id="content">
    <h1>Today's Git Log</h1>
    <table id="jsonTable" class="display">
        <thead>
        </thead>
        <tbody>
        </tbody>
    </table>
</div>

<script src="jquery-3.6.4.js"></script>

<script>
    var jsonData= {{git_log_list}};
    var headers = Object.keys(jsonData[0]);
    
    var tableHeaders = headers.map(header => "<th>" + header + "</th>").join("");
    $('#jsonTable thead').html("<tr>" + tableHeaders + "</tr>");
    
    var tableRows = jsonData.map(data => {
        return "<tr>" + headers.map(header => "<td>" + data[header] + "</td>").join("") + "</tr>";
    }).join("");
    
    $('#jsonTable tbody').html(tableRows);
    
    $('#jsonTable').DataTable({
        searching: true
    });
    
    function goBack() {
        window.location.href = "performance_report.html";
    }
    
</script>
</body>
</html>

"""