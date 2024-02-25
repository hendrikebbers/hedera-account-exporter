<!DOCTYPE html>
<html lang="en">
<#include "common/head.ftlh">
<body>
<#include "common/bodyStart.ftlh">
<main class="container">
    <#include "common/menu.ftlh">
  <section>
    <h2>Home</h2>
    <p class="lead">
    Wert HBAR: ${exchangeRate}
    <#if eurAmount?has_content></br>Aktueller Kontostand: ${eurAmount}</#if>
    <#if stackedHbar?has_content></br>Anzahl Staking: ${stackedHbar}</#if>
    <#if stackedEur?has_content></br>Wert Staking: ${stackedEur}</#if>
    </p>
  </section>
</main>
</body>
</html>