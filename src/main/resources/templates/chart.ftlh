<!DOCTYPE html>
<html lang="en">
<#include "common/head.ftlh">
<body>
<#include "common/bodyStart.ftlh">
<main class="container">
    <#include "common/menu.ftlh">
  <section>
    <h2>Kontoverlauf</h2>
    <#if type == 'hbar'>
    <canvas id="hbarChart"></canvas>
    <#else>
    <canvas id="eurChart"></canvas>
    </#if>
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
      if(document.getElementById("hbarChart") != null) {
        var hbarChartCtx = document.getElementById("hbarChart").getContext("2d");
        new Chart(hbarChartCtx, hbarChartConfig);
      }
      if(document.getElementById("eurChart") != null) {
        var eurChartCtx = document.getElementById("eurChart").getContext("2d");
        new Chart(eurChartCtx, eurChartConfig);
      }
    </script>
  </section>
</main>
</body>
</html>