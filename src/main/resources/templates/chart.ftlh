<!DOCTYPE html>
<html lang="en">
<#include "common/head.ftlh">
<body>
<main class="container">
    <#include "common/menu.ftlh">
  <section>
    <h2>Kontoverlauf</h2>
    <canvas id="hbarChart"></canvas>
    <canvas id="eurChart"></canvas>
    <script src="https://cdn.jsdelivr.net/npm/moment@2.30.1/moment.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chartjs-adapter-moment@1.0.1/dist/chartjs-adapter-moment.min.js"></script>
    <script>
      var numberFormatOption = {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      };

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
            x: {
              type: 'time'
            },
            y: {
              ticks: {
                callback: function(value, index, ticks) {
                  return new Intl.NumberFormat('de-DE', numberFormatOption).format(value,) + ' ℏ';
                }
              }
            }
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
            x: {
              type: 'time'
            },
            y: {
              ticks: {
                callback: function(value, index, ticks) {
                  return new Intl.NumberFormat('de-DE', numberFormatOption).format(value,) + ' €';
                }
              }
            }
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
<#include "common/bodyEnd.ftlh">
</body>
</html>