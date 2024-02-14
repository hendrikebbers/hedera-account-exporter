<!DOCTYPE html>
<html lang="en">
<header>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@3.4.1/dist/css/bootstrap.min.css" integrity="sha384-HSMxcRTRxnN+Bdg0JdbxYKrThecOKuH5zCYotlSAcp1+c8xmyTe9GYg1l9a69psu" crossorigin="anonymous">
</header>
<body>
<main class="container">
  <section>
    <h2>Kontoverlauf</h2>
    <canvas id="hbarChart"></canvas>
    <canvas id="eurChart"></canvas>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.1.0/Chart.bundle.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.13.0/moment.min.js"></script>
    <script>
      var hbarChartConfig = {
        type: 'line',
        data: {
          labels: ${xValues?no_esc},
          datasets: [{
            label: 'HBAR',
            data: ${hbarValues?no_esc}
          }]
        },
        options: {
          scales: {
            xAxes: [{
              type: 'time'
            }],
          },
        }
      };

      var eurChartConfig = {
        type: 'line',
        data: {
          labels: ${xValues?no_esc},
          datasets: [{
            label: 'EURO',
            data: ${eurValues?no_esc}
          }]
        },
        options: {
          scales: {
            xAxes: [{
              type: 'time'
            }],
          },
        }
      };

      var hbarChartCtx = document.getElementById("hbarChart").getContext("2d");
      new Chart(hbarChartCtx, hbarChartConfig);

      var eurChartCtx = document.getElementById("eurChart").getContext("2d");
      new Chart(eurChartCtx, eurChartConfig);

    </script>
  </section>
</main>
</body>
</html>