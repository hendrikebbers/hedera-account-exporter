<!DOCTYPE html>
<html lang="en">
<#include "common/head.ftlh">
<body>
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
      </div>
      <input type="submit" value="Save" />
    </form>
  </section>
</main>
<#include "common/bodyEnd.ftlh">
</body>
</html>