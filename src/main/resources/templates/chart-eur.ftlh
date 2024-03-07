<!DOCTYPE html>
<html lang="en">
<#include "common/head.ftlh">
<body>
<#include "common/bodyStart.ftlh">
<main class="container">
    <#include "common/menu.ftlh">
  <section>
    <h2>${headline}</h2>
    <canvas id="canvas"></canvas>
    <script>
      var numberFormatOption = {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      };
      var chartConfig = {
        type: 'line',
        data: {
          labels: ${xValues?no_esc},
          datasets: [{
            label: 'EUR',
            data: ${yValues?no_esc}
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
      var chartCtx = document.getElementById("canvas").getContext("2d");
      new Chart(chartCtx, chartConfig);
    </script>
  </section>
</main>
</body>
</html>