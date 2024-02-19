<!DOCTYPE html>
<html lang="en">
<#include "common/head.ftlh">
<body>
<main class="container">
    <#include "common/menu.ftlh">
  <section>
    <h2>Transaktionsübersicht</h2>
    <table class="table table-hover">
      <tr>
        <th>Datum</th>
        <th class="text-right">Betrag (HBAR)</th>
        <th class="text-right">Betrag (EUR)</th>
        <th class="text-right">Fifo (EUR)</th>
        <th class="text-right">Kummulative Anschaffungskosten (EUR)</th>
        <th>Anmerkung</th>
        <th class="text-right">Betrag Gesamt (HBAR)</th>
        <th class="text-right">Betrag Gesamt (EUR)</th>
        <th>Aktionen</th>
      </tr>
      <#list transactions as transaction>
      <tr>
        <td>${transaction.timestamp()}</td>
        <td class="text-right">${transaction.hbarAmount()}</td>
        <td class="text-right">${transaction.eurAmount()}</td>
        <td class="text-right">${transaction.fifoInEur()}</td>
        <td  class="text-right">${transaction.cumulativeCostInEur()}</td>
        <td>${transaction.note()}</td>
        <td class="text-right">${transaction.hbarBalanceAfterTransaction()}</td>
        <td class="text-right"><strong>${transaction.eurBalanceAfterTransaction()}</strong></td>
        <td><a class="btn btn-default" href="${'/transaction/' + transaction.id()}" role="button">edit</a><a class="btn btn-default" href="${transaction.hederaTransactionLink()}" target="_blank" role="button">details</a></td>
      </tr>
      </#list>
    </table>
  </section>
</main>
<#include "common/bodyEnd.ftlh">
</body>
</html>