const API_BASE = window.location.origin;
const toastEl = document.getElementById('toast');
const connStatus = document.getElementById('conn-status');
let toastTimer = null;

// ==================== i18n 翻译表 ====================
const I18N = {
  zh: {
    app_title: '密码工坊', backend_checking: '正在检测后端连接...',
    tab_random: '随机密码', tab_passphrase: '短语密码', tab_hash: '哈希计算', tab_analyze: '强度分析',
    pw_length: '密码长度', opt_lower: '小写字母 (a-z)', opt_upper: '大写字母 (A-Z)',
    opt_digits: '数字 (0-9)', opt_special: '特殊符号 (!@#...)', opt_avoid: '排除易混字符 (0O1lI)',
    btn_generate_pw: '生成随机密码', entropy_full: '熵值 (Entropy)', entropy: '熵值', verify_strength: '验证强度',
    pp_count: '单词数量', pp_sep: '分隔符', sep_hyphen: '连字符 -', sep_underscore: '下划线 _',
    sep_space: '空格', sep_dot: '句点 .', sep_none: '无分隔符', opt_title: '首字母大写 (Title Case)',
    opt_number: '随机插入数字', btn_generate_pp: '生成短语密码', candidates: '候选结果（共 5 条）',
    hash_input_label: '输入内容', hash_placeholder: '在此输入要计算哈希的文本...',
    hash_hint: '支持任意字符，按 UTF-8 编码计算', btn_hash: '计算哈希值',
    analyze_label: '待分析密码', analyze_placeholder: '输入密码进行强度与破解时间分析...',
    analyze_hint: '分析结果基于字符集/词库与长度的理论估算', btn_analyze: '分析强度',
    common_warn: '⚠️ 此密码在常见弱密码黑名单中，可被瞬间破解！请立即更换。',
    len: '长度', rating: '评级', crack_title: '预计破解所需时间',
    th_scene: '攻击场景', th_time: '预计耗时', th_algo: '算法', th_hash: '哈希值',
    backend_ok: '后端已连接', backend_fail: '⚠️ 后端未连接（请确认 app.py 已启动）',
    toast_copied: '已复制到剪贴板', toast_need_input: '请输入内容', toast_need_pw: '请输入密码',
    strength_very_weak: '极弱', strength_weak: '弱', strength_fair: '一般',
    strength_strong: '强', strength_very_strong: '很强', strength_extreme: '极强',
    scene_online_100ph: '在线限速 (100次/小时)', scene_online_10pm: '在线限速 (10次/分钟)',
    scene_cpu_md5: '单核 CPU (MD5)', scene_cpu_sha256: '单核 CPU (SHA-256)',
    scene_gpu_md5: 'GPU 集群 (MD5)', scene_gpu_sha256: 'GPU 集群 (SHA-256)',
    scene_gpu_bcrypt: 'GPU 集群 (bcrypt)', scene_asic_sha256: 'ASIC/专用机 (SHA-256)',
    scene_dict_rule: '常用字典攻击 (词库+规则)',
    unit_ms: '毫秒', unit_s: '秒', unit_min: '分钟', unit_h: '小时', unit_d: '天',
    unit_y: '年', unit_ky: '千年', unit_by: '十亿年',
    copy_title: '复制', verify_title: '验证强度',
    lang_name: '中', lang_switch: 'Switch to English',
  },
  en: {
    app_title: 'Password Forge', backend_checking: 'Checking backend connection...',
    tab_random: 'Random', tab_passphrase: 'Passphrase', tab_hash: 'Hash', tab_analyze: 'Analyze',
    pw_length: 'Password Length', opt_lower: 'Lowercase (a-z)', opt_upper: 'Uppercase (A-Z)',
    opt_digits: 'Digits (0-9)', opt_special: 'Symbols (!@#...)', opt_avoid: 'No Ambiguous Chars (0O1lI)',
    btn_generate_pw: 'Generate Password', entropy_full: 'Entropy', entropy: 'Entropy', verify_strength: 'Verify Strength',
    pp_count: 'Word Count', pp_sep: 'Separator', sep_hyphen: 'Hyphen -', sep_underscore: 'Underscore _',
    sep_space: 'Space', sep_dot: 'Dot .', sep_none: 'None', opt_title: 'Title Case',
    opt_number: 'Insert Number', btn_generate_pp: 'Generate Passphrase', candidates: 'Candidates (5)',
    hash_input_label: 'Input', hash_placeholder: 'Enter text to hash...',
    hash_hint: 'Supports any characters, UTF-8 encoded', btn_hash: 'Calculate Hash',
    analyze_label: 'Password', analyze_placeholder: 'Enter a password to analyze...',
    analyze_hint: 'Estimated from charset/wordlist and length', btn_analyze: 'Analyze',
    common_warn: '⚠️ This password is in the common weak-password list and can be cracked instantly! Change it now.',
    len: 'Length', rating: 'Rating', crack_title: 'Estimated Crack Time',
    th_scene: 'Attack Scenario', th_time: 'Est. Time', th_algo: 'Algorithm', th_hash: 'Hash Value',
    backend_ok: 'Backend connected', backend_fail: '⚠️ Backend not connected (run app.py)',
    toast_copied: 'Copied to clipboard', toast_need_input: 'Please enter text', toast_need_pw: 'Please enter a password',
    strength_very_weak: 'Very Weak', strength_weak: 'Weak', strength_fair: 'Fair',
    strength_strong: 'Strong', strength_very_strong: 'Very Strong', strength_extreme: 'Extreme',
    scene_online_100ph: 'Online throttled (100/h)', scene_online_10pm: 'Online throttled (10/min)',
    scene_cpu_md5: 'CPU single-core (MD5)', scene_cpu_sha256: 'CPU single-core (SHA-256)',
    scene_gpu_md5: 'GPU cluster (MD5)', scene_gpu_sha256: 'GPU cluster (SHA-256)',
    scene_gpu_bcrypt: 'GPU cluster (bcrypt)', scene_asic_sha256: 'ASIC hardware (SHA-256)',
    scene_dict_rule: 'Dictionary + rules attack',
    unit_ms: 'ms', unit_s: 's', unit_min: 'min', unit_h: 'h', unit_d: 'days',
    unit_y: 'years', unit_ky: 'thousand yrs', unit_by: 'billion yrs',
    copy_title: 'Copy', verify_title: 'Verify strength',
    lang_name: 'EN', lang_switch: '切换为中文',
  },
};

let lang = 'zh';
try { lang = localStorage.getItem('pw_lang') || 'zh'; } catch(e) {}

function t(key) { return (I18N[lang] && I18N[lang][key]) || I18N.zh[key] || key; }

function applyLang() {
  document.querySelectorAll('[data-i18n]').forEach(el => {
    el.textContent = t(el.dataset.i18n);
  });
  document.querySelectorAll('[data-i18n-ph]').forEach(el => {
    el.setAttribute('placeholder', t(el.dataset.i18nPh));
  });
  const btn = document.getElementById('lang-btn');
  btn.textContent = t('lang_name');
  btn.title = t('lang_switch');
  document.querySelectorAll('.icon-btn[title]').forEach(el => {
    if (el.title === '复制' || el.title === 'Copy' || el.title === '验证强度' || el.title === 'Verify strength') {
      el.title = t(el.id === 'copy-pw' ? 'copy_title' : 'verify_title');
    }
  });
  // 重新渲染动态内容
  renderDynamic();
}

function switchLang() {
  lang = (lang === 'zh') ? 'en' : 'zh';
  try { localStorage.setItem('pw_lang', lang); } catch(e) {}
  applyLang();
}

// 语言按钮
document.getElementById('lang-btn').addEventListener('click', switchLang);

// ==================== 强度评级（本地计算 + 翻译） ====================
function strengthInfo(entropy) {
  if (entropy < 28) return { key: 'very_weak', color: '#cf3a3a' };
  if (entropy < 36) return { key: 'weak', color: '#e67e22' };
  if (entropy < 60) return { key: 'fair', color: '#f1c40f' };
  if (entropy < 80) return { key: 'strong', color: '#2ecc71' };
  if (entropy < 128) return { key: 'very_strong', color: '#27ae60' };
  return { key: 'extreme', color: '#1abc9c' };
}

// ==================== 时间格式化（多语言） ====================
function formatDuration(seconds) {
  const U = I18N[lang];
  if (seconds < 1) return (seconds * 1000).toFixed(1) + ' ' + U.unit_ms;
  if (seconds < 60) return seconds.toFixed(1) + ' ' + U.unit_s;
  if (seconds < 3600) return (seconds / 60).toFixed(1) + ' ' + U.unit_min;
  if (seconds < 86400) return (seconds / 3600).toFixed(1) + ' ' + U.unit_h;
  if (seconds < 86400 * 365) return (seconds / 86400).toFixed(1) + ' ' + U.unit_d;
  if (seconds < 86400 * 365 * 100) return (seconds / (86400 * 365)).toFixed(1) + ' ' + U.unit_y;
  if (seconds < 86400 * 365 * 1e6) return (seconds / (86400 * 365 * 1e3)).toFixed(1) + ' ' + U.unit_ky;
  if (seconds < 86400 * 365 * 1e9) return (seconds / (86400 * 365 * 1e9)).toFixed(1) + ' ' + U.unit_by;
  return (seconds / (86400 * 365)).toExponential(2) + ' ' + U.unit_y;
}

const SCENE_NAMES = {
  online_100ph: 'scene_online_100ph', online_10pm: 'scene_online_10pm',
  cpu_md5: 'scene_cpu_md5', cpu_sha256: 'scene_cpu_sha256',
  gpu_md5: 'scene_gpu_md5', gpu_sha256: 'scene_gpu_sha256',
  gpu_bcrypt: 'scene_gpu_bcrypt', asic_sha256: 'scene_asic_sha256',
  dict_rule: 'scene_dict_rule',
};

// ==================== 工具函数 ====================
function showToast(msg) {
  toastEl.textContent = msg;
  toastEl.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toastEl.classList.remove('show'), 2200);
}

async function copy(text) {
  try {
    await navigator.clipboard.writeText(text);
    showToast(t('toast_copied'));
  } catch(e) {
    const ta = document.createElement('textarea');
    ta.value = text;
    document.body.appendChild(ta);
    ta.select();
    document.execCommand('copy');
    document.body.removeChild(ta);
    showToast(t('toast_copied'));
  }
}

async function safeJson(r) {
  const ct = r.headers.get('content-type') || '';
  if (!ct.includes('application/json')) {
    const text = await r.text();
    throw new Error('HTTP ' + r.status + ' (not JSON)');
  }
  if (!r.ok) {
    const d = await r.json().catch(() => ({}));
    throw new Error(d.error || 'HTTP ' + r.status);
  }
  return r.json();
}

function escapeHtml(t) {
  const map = {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'};
  return (t || '').toString().replace(/[&<>"']/g, m => map[m]);
}

function setBadge(el, key, color) {
  el.textContent = t('strength_' + key);
  el.style.background = color + '22';
  el.style.color = color;
}

function setBar(el, entropy, color) {
  el.style.width = Math.min(100, (entropy / 128) * 100) + '%';
  el.style.background = color;
}

// ==================== 后端连接检测 ====================
(async function checkBackend() {
  try {
    const r = await fetch(API_BASE + '/api/health');
    const d = await safeJson(r);
    connStatus.textContent = t('backend_ok') + ' · ' + d.version;
    connStatus.style.color = '#2ecc71';
  } catch(e) {
    connStatus.textContent = t('backend_fail');
    connStatus.style.color = '#cf3a3a';
  }
})();

// ==================== 主题切换 ====================
const themes = {
  purple: { p:'#6750A4', s:'#625B71', pc:'#EADDFF', sc:'#E8DEF8' },
  blue:   { p:'#005AC1', s:'#4A5C7A', pc:'#D6E3FF', sc:'#D9E3F8' },
  green:  { p:'#006E2A', s:'#52634F', pc:'#6CFF91', sc:'#D5E8D0' },
  rose:   { p:'#B5004C', s:'#77565D', pc:'#FFD9DF', sc:'#FFD9DF' },
  orange: { p:'#964900', s:'#7A5832', pc:'#FFDCC6', sc:'#FCDEBB' },
};
document.querySelectorAll('.theme-chip').forEach(chip => {
  chip.addEventListener('click', () => {
    document.querySelectorAll('.theme-chip').forEach(c => c.classList.remove('active'));
    chip.classList.add('active');
    const t = themes[chip.dataset.theme], r = document.documentElement;
    r.style.setProperty('--md-primary', t.p);
    r.style.setProperty('--md-secondary', t.s);
    r.style.setProperty('--md-primary-container', t.pc);
    r.style.setProperty('--md-secondary-container', t.sc);
  });
});

// ==================== Tab 切换 ====================
document.querySelectorAll('.tab-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
    btn.classList.add('active');
    document.getElementById('tab-' + btn.dataset.target).classList.add('active');
  });
});

// ==================== 一键验证强度 ====================
function analyzePasswordNow(pw) {
  document.getElementById('analyze-input').value = pw;
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
  document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
  document.querySelector('.tab-btn[data-target="analyze"]').classList.add('active');
  document.getElementById('tab-analyze').classList.add('active');
  document.getElementById('btn-analyze').click();
}

// ==================== 随机密码 ====================
const pwLen = document.getElementById('pw-len');
const pwLenVal = document.getElementById('pw-len-val');
pwLen.addEventListener('input', () => pwLenVal.textContent = pwLen.value);
pwLenVal.textContent = pwLen.value || 16;

document.querySelectorAll('#pw-options .check-item').forEach(label => {
  label.addEventListener('click', (e) => {
    if (e.target.tagName !== 'INPUT') {
      const cb = label.querySelector('input');
      cb.checked = !cb.checked;
    }
    label.classList.toggle('checked', label.querySelector('input').checked);
  });
});

document.getElementById('btn-gen-pw').addEventListener('click', async () => {
  const opts = {};
  document.querySelectorAll('#pw-options input').forEach(cb => opts[cb.dataset.key] = cb.checked);
  try {
    const r = await fetch(API_BASE + '/api/generate-password', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ length: parseInt(pwLen.value) || 16, ...opts })
    });
    const d = await safeJson(r);
    document.getElementById('pw-result').style.display = 'block';
    document.getElementById('pw-text').textContent = d.password;
    document.getElementById('pw-entropy').textContent = d.entropy + ' bits';
    const si = strengthInfo(d.entropy);
    setBadge(document.getElementById('pw-badge'), si.key, si.color);
    setBar(document.getElementById('pw-bar'), d.entropy, si.color);
  } catch(e) { showToast(t('btn_generate_pw') + ': ' + e.message); }
});

document.getElementById('copy-pw').addEventListener('click', () => {
  const t = document.getElementById('pw-text').textContent;
  if (t) copy(t);
});
document.getElementById('verify-pw').addEventListener('click', () => {
  const t = document.getElementById('pw-text').textContent;
  if (t) analyzePasswordNow(t);
});

// ==================== 短语密码 ====================
const ppCount = document.getElementById('pp-count');
const ppCountVal = document.getElementById('pp-count-val');
ppCount.addEventListener('input', () => ppCountVal.textContent = ppCount.value);
ppCountVal.textContent = ppCount.value || 6;

let currentSep = '-';
document.querySelectorAll('#pp-sep .sep-chip').forEach(chip => {
  chip.addEventListener('click', () => {
    document.querySelectorAll('#pp-sep .sep-chip').forEach(c => c.classList.remove('active'));
    chip.classList.add('active');
    currentSep = chip.dataset.sep;
  });
});

let lastPhrases = [];

document.getElementById('btn-gen-pp').addEventListener('click', async () => {
  try {
    const r = await fetch(API_BASE + '/api/generate-passphrase', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        count: parseInt(ppCount.value) || 6,
        separator: currentSep,
        include_number: document.getElementById('pp-num').checked,
        title_case: document.getElementById('pp-title').checked
      })
    });
    const d = await safeJson(r);
    lastPhrases = d.passphrases;
    renderDynamic();
    document.getElementById('pp-result').style.display = 'block';
    document.getElementById('pp-entropy').textContent = d.entropy + ' bits';
    const si = strengthInfo(d.entropy);
    setBadge(document.getElementById('pp-badge'), si.key, si.color);
  } catch(e) { showToast(t('btn_generate_pp') + ': ' + e.message); }
});

// ==================== 哈希 ====================
let lastHashes = null;

document.getElementById('btn-hash').addEventListener('click', async () => {
  const val = document.getElementById('hash-input').value;
  if (!val) { showToast(t('toast_need_input')); return; }
  try {
    const r = await fetch(API_BASE + '/api/hash', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ password: val })
    });
    const d = await safeJson(r);
    lastHashes = d.hashes;
    renderDynamic();
    document.getElementById('hash-result').style.display = 'block';
  } catch(e) { showToast(t('btn_hash') + ': ' + e.message); }
});

// ==================== 分析 ====================
let lastAnalysis = null;

document.getElementById('btn-analyze').addEventListener('click', async () => {
  const val = document.getElementById('analyze-input').value;
  if (!val) { showToast(t('toast_need_pw')); return; }
  try {
    const r = await fetch(API_BASE + '/api/analyze', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ password: val })
    });
    const d = await safeJson(r);
    lastAnalysis = d;
    renderDynamic();
    document.getElementById('analyze-result').style.display = 'block';
  } catch(e) { showToast(t('btn_analyze') + ': ' + e.message); }
});

// ==================== 动态内容渲染（随语言变化刷新） ====================
function renderDynamic() {
  // 短语候选列表
  const container = document.getElementById('pp-list');
  if (container && lastPhrases.length > 0) {
    container.innerHTML = '';
    lastPhrases.forEach(phrase => {
      const div = document.createElement('div');
      div.className = 'list-item';
      div.innerHTML = '<span>' + escapeHtml(phrase) + '</span><div class="item-actions">'
        + '<button class="icon-btn analyze-small" title="' + t('verify_title') + '"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg></button>'
        + '<button class="icon-btn copy-small" title="' + t('copy_title') + '"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg></button>'
        + '</div>';
      div.querySelector('.copy-small').addEventListener('click', () => copy(phrase));
      div.querySelector('.analyze-small').addEventListener('click', () => analyzePasswordNow(phrase));
      container.appendChild(div);
    });
  }

  // 哈希结果表
  const tbl = document.getElementById('hash-table');
  if (tbl && lastHashes) {
    let html = '<tr><th>' + t('th_algo') + '</th><th>' + t('th_hash') + '</th></tr>';
    for (const [algo, hash] of Object.entries(lastHashes)) {
      html += '<tr><td class="mono">' + escapeHtml(algo) + '</td><td class="mono" style="word-break:break-all">' + escapeHtml(hash) + '</td></tr>';
    }
    tbl.innerHTML = html;
  }

  // 分析结果
  const warn = document.getElementById('common-warn');
  if (lastAnalysis) {
    const d = lastAnalysis;
    warn.style.display = d.common_password ? 'block' : 'none';
    document.getElementById('an-len').textContent = d.length + ' ' + (lang === 'zh' ? '位' : 'chars');
    document.getElementById('an-entropy').textContent = d.entropy + ' bits';
    const si = strengthInfo(d.entropy);
    setBadge(document.getElementById('an-badge'), si.key, si.color);
    setBar(document.getElementById('an-bar'), d.entropy, si.color);
    const ctbl = document.getElementById('crack-table');
    let chtml = '<tr><th>' + t('th_scene') + '</th><th>' + t('th_time') + '</th></tr>';
    for (const [scene, seconds] of Object.entries(d.crack_times)) {
      const name = SCENE_NAMES[scene] ? t(SCENE_NAMES[scene]) : scene;
      chtml += '<tr><td>' + escapeHtml(name) + '</td><td class="mono" style="font-weight:700">' + escapeHtml(formatDuration(seconds)) + '</td></tr>';
    }
    ctbl.innerHTML = chtml;
  }
}

// ==================== 初始化 ====================
applyLang();
