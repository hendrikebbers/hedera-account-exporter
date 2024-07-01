<!DOCTYPE html>
<html lang="en">
<#include "common/head.ftlh">
<body>
<#include "common/bodyStart.ftlh">
<main class="container">
    <#include "common/menu.ftlh">
  <section>
    <h2>Transaktion</h2>
    <form action="${'/transaction/' + transaction.id()}" method="POST">
      <div class="form-group">
        <label for="hederaTransactionId">Hedera Transaction ID</label>
        <input type="text" class="form-control" id="hederaTransactionId" name="hederaTransactionId" value="${transaction.hederaTransactionId()}" readonly />
        <label for="note">Note</label>
        <input type="text" class="form-control" id="note" name="note" value="${transaction.note()}"  />
        <input type="text" class="form-control" id="exchangeRate" name="exchangeRate" value="${transaction.exchangeRate()}"  />
      </div>
      <input type="submit" value="Save" />
    </form>
  </section>
</main>
</body>
</html>