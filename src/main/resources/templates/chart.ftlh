<!DOCTYPE html>
<html lang="en">
<#include "common/head.ftlh">
<body>
<#include "common/bodyStart.ftlh">
<main class="container">
    <#include "common/menu.ftlh">
  <section>
    <h2>Kontoverlauf</h2>
    <canvas id="canvas"></canvas>
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
      var stakingChartConfig = {
        type: 'line',
        data: {
          labels: ${xValues?no_esc},
          datasets: [{
            label: 'HBAR Staking',
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
                  return new Intl.NumberFormat('de-DE', numberFormatOption).format(value,) + ' ℏ';
                }
              }
            }
          },
        }
      };
      var chartCtx = document.getElementById("canvas").getContext("2d");
      <#if type == 'hbar'>
      new Chart(chartCtx, hbarChartConfig);
      </#if>
      <#if type == 'eur'>
      new Chart(chartCtx, eurChartConfig);
      </#if>
      <#if type == 'staking'>
      new Chart(chartCtx, stakingChartConfig);
      </#if>
    </script>
  </section>
</main>
</body>
</html>