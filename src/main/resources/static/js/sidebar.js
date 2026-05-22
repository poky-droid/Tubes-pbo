function renderSidebar(activePage) {
  const links = [
    { href: 'Dashboard',     icon: 'bi-speedometer2',        label: 'Dashboard' },
    { href: 'kendaraan', icon: 'bi-car-front-fill',      label: 'Kendaraan' },
    { href: 'penjualan', icon: 'bi-receipt-cutoff',      label: 'Penjualan' },
    { href: 'testdrive', icon: 'bi-key-fill',            label: 'Test Drive' },
    { href: 'pembeli',   icon: 'bi-people-fill',         label: 'Pembeli' },
    { href: 'laporan',   icon: 'bi-file-earmark-bar-graph-fill', label: 'Laporan' },
    { href: 'profil',    icon: 'bi-person-badge-fill',   label: 'Profil Owner' },
  ];

  const mainLinks = links.slice(0, 5);
  const settingLinks = links.slice(5);

  function li(l) {
    const active = l.href === activePage ? 'active' : '';
    return `<a href="${l.href}" class="nav-link ${active}"><i class="bi ${l.icon}"></i> ${l.label}</a>`;
  }

  document.getElementById('sidebar').innerHTML = `
    <div class="sidebar-brand">
      <div class="brand-name">AutoPrime</div>
      <div class="brand-sub">Showroom Management</div>
    </div>
    <nav class="sidebar-nav mt-1">
      <div class="nav-label">Menu Utama</div>
      ${mainLinks.map(li).join('')}
      <div class="nav-label mt-2">Pengaturan</div>
      ${settingLinks.map(li).join('')}
    </nav>
    <div class="sidebar-footer">
      <div class="sidebar-user">
        <div class="s-avatar">A</div>
        <div>
          <div class="s-name">Admin Owner</div>
          <div class="s-role">Owner · Super Admin</div>
        </div>
      </div>
    </div>
  `;
}
