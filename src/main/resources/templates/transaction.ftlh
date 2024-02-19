<!DOCTYPE html>
<html lang="en">
<header>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@3.4.1/dist/css/bootstrap.min.css" integrity="sha384-HSMxcRTRxnN+Bdg0JdbxYKrThecOKuH5zCYotlSAcp1+c8xmyTe9GYg1l9a69psu" crossorigin="anonymous">
</header>
<body>
<main class="container">
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
</body>
</html>