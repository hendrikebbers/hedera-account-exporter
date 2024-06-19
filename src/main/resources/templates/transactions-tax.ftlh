<!DOCTYPE html>
<html lang="en">
<#include "common/head.ftlh">
<body>
<#include "common/bodyStart.ftlh">
<main class="container">
    <#include "common/menu.ftlh">
  <section>
    <h2>Transaktionsübersicht Steuer</h2>
    <table class="table table-hover">
      <tr>
        <th>Index</th>
        <th>Datum</th>
        <th class="text-right">Betrag (HBAR)</th>
        <th>Wechselkurs</th>
        <th class="text-right">Betrag (EUR)</th>
        <th class="text-right">Fifo (EUR)</th>
        <th class="text-right">Fifo Zuordnung</th>
        <th class="text-right">Anschaffungskosten<br>Kummulativ (EUR)</th>
        <th>Anmerkung</th>
        <th class="text-right">Betrag Gesamt (HBAR)</th>
      </tr>
        <#list transactions as transaction>
          <tr>
            <td>${transaction.index()}</td>
            <td>${transaction.timestamp()}</td>
            <td class="text-right">${transaction.hbarAmount()}</td>
            <td>${transaction.exchangeRate()}</td>
            <td class="text-right">${transaction.eurAmount()}</td>
            <td class="text-right">${transaction.fifoInEur()}</td>
            <td>
              <div style="white-space:pre;">${transaction.fifoUsage()}</div>
            </td>
            <td class="text-right">${transaction.cumulativeCostInEur()}</td>
            <td>${transaction.note()}</td>
            <td class="text-right">${transaction.hbarBalanceAfterTransaction()}</td>
          </tr>
        </#list>
    </table>
  </section>
</main>
</body>
</html>