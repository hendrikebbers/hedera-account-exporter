<nav class="navbar navbar-expand-lg bg-body-tertiary">
  <div class="container-fluid">
    <a class="navbar-brand" href="/">Crypto Konto</a>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
            aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
      <span class="navbar-toggler-icon"></span>
    </button>
      <#if accountId??>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav">
            <li class="nav-item">
              <a class="nav-link" href="/transactions">Transaktionen</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/transactions-tax">Transaktionen (Druck)</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/chart?type=eur">Euro Chart</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/chart?type=hbar">Hbar Chart</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/chart?type=stakingEur">Staking Euro</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/chart?type=stakingHbar">Staking Hbar</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/chart?type=exchange">Wechselkurs</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/logout">Logout</a>
            </li>
          </ul>
        </div>
      </#if>
  </div>
</nav>