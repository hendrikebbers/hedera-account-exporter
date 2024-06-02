<!DOCTYPE html>
<html lang="en">
<#include "common/head.ftlh">
<body>
<#include "common/bodyStart.ftlh">
<main class="container">
    <#include "common/menu.ftlh">
  <section>
    <h2>Transaktionsübersicht</h2>
    <table class="table table-hover">
      <tr>
        <th>Datum</th>
        <th class="text-right">Betrag (HBAR)</th>
        <th class="text-right">Betrag (EUR)</th>
        <#if print == true>
          <th class="text-right">Kurs (EUR)</th>
          <th class="text-right">Fifo (EUR)</th>
          <th class="text-right">Anschaffungskosten<br>Kummulativ (EUR)</th>
        </#if>
        <th>Anmerkung</th>
        <th class="text-right">Betrag Gesamt (HBAR)</th>
        <#if print == false>
          <th class="text-right">Betrag Gesamt (EUR)</th>
          <th>Aktionen</th>
        </#if>
      </tr>
      <#list transactions as transaction>
      <tr>
        <td>${transaction.timestamp()}</td>
        <td class="text-right">${transaction.hbarAmount()}</td>
        <td class="text-right">${transaction.eurAmount()}</td>
        <#if print == true>
          <td class="text-right">${transaction.exchangeRate()}</td>
          <td class="text-right">${transaction.fifoInEur()}</td>
          <td  class="text-right">${transaction.cumulativeCostInEur()}</td>
        </#if>
        <td>${transaction.note()}</td>
        <td class="text-right">${transaction.hbarBalanceAfterTransaction()}</td>
        <#if print == false>
          <td class="text-right"><strong>${transaction.eurBalanceAfterTransaction()}</strong></td>
          <td><a class="btn btn-default" href="${'/transaction/' + transaction.id()}" role="button">edit</a><a class="btn btn-default" href="${transaction.hederaTransactionLink()}" target="_blank" role="button">details</a></td>
        </#if>
      </tr>
      </#list>
    </table>
  </section>
</main>
</body>
</html>