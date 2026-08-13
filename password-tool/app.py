#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
🔐 密码工坊 - 多文件版 v4.3
============================================
包含: 随机密码 / 短语密码 / 哈希计算 / 强度与破解时间分析
Material 3 (Google Material Web) 风格界面

文件结构:
    app.py                 后端 (Flask API)
    templates/index.html   前端模板
    static/style.css       样式 (M3 设计令牌)
    static/script.js       交互逻辑
    static/vendor/         Material 3 组件库 (运行 setup_material.sh 自动安装)

用法:
    pip install flask
    bash setup_material.sh   # 首次运行，安装 M3 UI 库
    python app.py
    浏览器打开 http://127.0.0.1:8080
============================================
"""

import os
import sys
import math
import secrets
import hashlib
import string

try:
    from flask import Flask, request, jsonify, render_template
except ImportError:
    print("[提示] 正在安装 Flask ...")
    os.system(f"{sys.executable} -m pip install flask -q")
    try:
        from flask import Flask, request, jsonify, render_template
    except ImportError:
        print("[失败] 请手动运行: pip install flask")
        sys.exit(1)

app = Flask(__name__)

# ========================== 2048 词表（BIP39 级） ==========================
WORD_LIST = """able about above absorb abstract abuse academy accent accept access accident
account accuse achieve acid acoustic acquire across act action activate active actor actual
adapt add addict address adjust admire admit adult advance advantage adventure advice aerobic
affair afford afraid after again age agent agree ahead aid aim air airport aisle alarm
album alert alien align alive all alley allow almost alone alpha already also alter always
amateur amaze among amount amused anchor ancient anger angle angry animal ankle announce annual
another answer antenna anxiety any apart apology appear apple apply april arch area argue armor
around arrange arrow art artist aspect assault asset assign assist assume assure asthma athlete
atom attack attend attitude attract auction august aunt author auto autumn average avoid awake
award aware away awful axis baby back bacon badge bag bake balance balcony ball bamboo banana
band bank banner bar bare bargain bark barn barrel base basic basin basis basket battle beach
bean bear beard beat beauty because become beef before begin behave behind being belief believe
bell belong below belt bench benefit best betray better between beyond bicycle bid bike bind
biology bird birth bitter black blade blame blank blast blaze bleak blend bless blind blink
block blood bloom blue blur blush board boast boat body boil bomb bone bonus book boost boot
border boring borrow boss both bother bottle bottom bounce boundary bowl box boy brain brake
branch brand brass brave bread breeze brick bridge brief bright bring broad bronze brother brown
brush bubble budget buffalo build bulb bulk bullet bundle bunker burden burger burst bush business
busy butter buyer cabin cable cactus cage cake calcium calendar call calm camel camera camp can
cancel cancer candidate candle canvas capable capital captain capture car carbon card care career
cargo carpet carry cart carve case cash casino cast castle casual cat catch category cattle cause
caution cave ceiling celery cell cement census center central century ceremony certain chain chair
chalk challenge chamber champion chance change channel chaos chapter charge charity charm chart chase
chat cheap check cheek cheer chef chest chicken chief child chill china chip chocolate choice
choose chronic chunk church cinema circle citizen city civil claim clarify clash class classic clay
clean clear clerk click client cliff climate climb clinic clip clock clone close cloth cloud clown
club clue cluster coach coal coast coat cobra code coffee coil coin cold collect college color
column combat combine come comedy comfort comic command comment commit common company compare compass
compete compile complain complete complex compose compute concept concert conduct confirm conflict connect
consent consider consist constant consume contact contain content contest context control convert convince
cook cool copy coral core corn corner correct cost cotton couch could council count country county
couple courage course court cousin cover crack craft crash crawl crazy cream create credit creek
crew cricket crime crisp critic crop cross crowd crucial cruel cruise crush crust cry cube cuisine
culture cup cupboard cure curious current curtain curve custom cycle dad damage dance danger daring
dark dash data date daughter dawn day dead deal dear death debate debt decade december decide decision
deck declare decorate decrease dedicate deer defeat defend define degree delay deliver delta demand demo
denial deny depart depend deposit depth deputy derive describe desert design desk despair despite destroy
detail detect develop device devote diagram dialog diamond diary dictate diesel diet differ digital dignity
dimension dinner direct dirt dirty disagree discover discuss disease dish disk dismiss display distance distant
divide divine doctor document dog dollar domain domestic dominant donate donkey door double doubt dove down
draft drag dragon drama draw dream dress drift drill drink drive driver drop drown drum dry duck
dumb during dust duty dwell dynamic each eagle ear early earn earth ease east easy echo ecology edge
edit educate effect effort egg eight either elder elect elegant element elephant elite else email embark embrace
emerge emotion empire employ empty enable enact encounter encourage end enemy energy engage engine enhance enjoy
enlist enough ensure enter entire entry envelope equal equip era erase escape essay essence estate eternal ethics
evidence evil evoke exact exam example exceed excel except excess exchange excite exclude excuse execute exercise exhaust
exhibit exist exit expand expect expense expert explain explode explore export expose express extend extent extra eye
fabric face factor factory faculty fade fail fair faith fall false fame family famous fan fancy far farm
farmer fashion fast fatal fate father fault favor fear feature federal fee feed feel fellow fence festival fetch
fever few fiber fiction field fierce fight figure file fill film filter final finance find fine finger finish
fire firm first fish fist fit five fix flag flame flash flat flavor flee flesh flex flight float flock
flood floor flour flow flower fluid flush fly foam focus fog fold folk follow food fool foot force forest
forever forget fork form formal format former formula fortune forum forward fossil foster found frame free freeze freight
frequent fresh friend fright frog from front frost fruit fuel full fully fun fund funny fur fury future gain
galaxy gallery game gap garage garden garlic gas gate gather gauge gear gender gene general genius gentle genuine
gesture ghost giant gift girl give glad glance glass glide global glory glove glow glue go goal goat gold
golden golf gone good govern grab grace grade gradual grain grand grant grape grass grateful grave gravity gray great
green greet grid grief grill grim grind grip grocery ground group grow guard guess guest guide guilt guilty guitar
gun guy gym habit hair half hall halo halt hand handle hang happen happy harbor hard harm harvest hat hatch
hate haunt have hawk hazard head heal health hear heart heat heaven heavy heel height hello help hence herb
here hero hidden hide high hill hint hip hire his history hit hobby hold hole holiday hollow holy home honest
honor hood hope horizon horn horror horse hospital host hot hotel hour house hover how huge human humor hundred
hunger hunt hurry hurt husband hybrid ice icon idea ideal identify idle ignite ignore ill image imagine impact imply
import impose impress improve impulse inch incident include income increase index indicate indoor industry infant inform inherit
initial inject injury inner innocent input inquiry insect insert inside insist inspire install instance instead insult insure intact
intake intend intense interest interior into introduce invest invite involve iron island isolate issue item ivory jacket jail
jam january jar jaw jazz jeans jelly jet jewel job join joint joke journal journey joy judge juice jump jungle
junior jury just justice keen keep kettle key kick kid kidney kill kind king kiss kit kitchen kite knee knife
knit knock knot know label labor lack ladder lady lake lamb lamp land landscape lane language large last late later
latest laugh launch law lawn layer lazy lead leader leaf league lean learn lease least leave lecture left leg legal
legend lemon lend length lens less lesson let letter level lever liar liberal library license lid lie life lift light
like likely limit line link lion lip liquid list listen liter little live load local locate lock logic logo lonely
long look loop loose lord lose loss lot loud love loyal luck lucky luggage lumber lunch lung luxury machine mad
magic magnet maid mail main maintain major make male mall man manage mandate mango manner manual many map marble march
margin marine mark market marriage marry mask mass master match mate material math matrix matter mature maximum maybe mayor
meal mean meaning measure meat mechanic medal media medical meet meeting member memory mental mention menu merchant mercy mere
merely merry mesh message metal method middle might mild mile milk mill mind mine minimum minister minor minus minute miracle
mirror miss mission mist mistake mix mobile mode model modern modest modify module moment money monitor monkey month mood moon
moral more morning mortal most mother motion motor mount mountain mouse mouth move movie much mud multiple murder muscle museum
mushroom music must mutual mystery myth nail name narrow nation native nature near nearby nearly neat neck need needle negative
neighbor nerve nest net network neutral never new news next nice night nine noble nod noise none noon normal north nose
note notice novel now nuclear number numerous nurse nut oak obey object observe obtain obvious occasion occupy occur ocean october
odd offer office officer official often oil okay old olive omit once one onion online only onset onto open operate opinion
oppose option orange orbit order ordinary organ organic orient origin other otherwise ought ounce out outcome outer output outset
oval oven over overall overcome owe own owner oxygen pace pack packet pad page pain paint pair palace pale palm pan
panel panic paper parade parent park parrot part partly partner party pass passage passion past patch path patient patrol pattern
pause pave pay peace peak peanut pear pearl peer pen penalty pencil people pepper per perfect perform perfume perhaps period permit
person personal persuade pet phase phone photo phrase physical piano pick picture piece pig pigeon pile pill pilot pin pine pink
pipe pit pitch pity place plain plan plane planet plant plastic plate play player please pleasure plenty plot plunge plus pocket
poem poet poetry point poison polar pole police policy polite political poll pollen pond pool poor popular port pose position
positive possess possible post pot potato pound pour powder power practical practice praise pray precious predict prefer pregnant prepare presence
present preserve press pretend pretty prevent previous price pride primary prime prince princess print prior prison privacy private prize probably
problem procedure process produce product professor profile profit program progress project promise promote prompt proof proper property propose protect protein
protest proud prove provide public pull pulse pump punch punish pure purple purpose push put puzzle pyramid qualify quality quantity
quarter queen question quick quiet quit quite quote rabbit race radar radio raft rage rail rain raise rally random range rank
rapid rare rate rather ratio raven raw ray reach react read reader ready real reality realize really reason recall receive recent
recipe reckon record recover recycle reduce refer refine reflect reform refuse regard regime region register regret regular reject relate relation relative
relax release relevant relief rely remain remark remote remove render renew rent repair repeat replace reply report reporter represent request require
rescue research resemble reserve resist resolve resort resource respect respond response rest restore result retain retire return reveal revenge revenue review
revise revival revolution reward rhythm rice rich rid ride ridge rifle right rigid ring riot rise risk ritual rival river road roast
rob robot rock rocket role roll roof room root rope rose rough round route routine row royal rub ruby ruin rule run
rural rush rust sacred sad safe safety sage sail saint sake salad salary sale salt same sample sand sandwich satisfy sauce save
saving say scale scan scare scarf scene scheme school science scope score scout scream screen script scroll sea search season seat second
secret section sector secure see seed seek seem segment seize select self sell send senior sense sentence separate sequence series serious serve
service session set setting settle seven several severe sew shade shadow shaft shake shall shallow shame shape share shark sharp sheet shelf
shell shelter shield shift shine ship shirt shiver shock shoe shoot shop shore short shot should shoulder shout show shower shut sick
side siege sight sign signal significance silent silk silly silver similar simple simply sin since sing single sink sir sister sit site
situation six size skate sketch ski skill skin skip skirt skull sky slab slam slap slave sleep slice slide slight slim slip slogan
slope slow small smart smash smell smile smoke smooth snack snake snap snow soap soccer social sock socket soda sofa soft soil
solar soldier solid solve some someone something sometimes somewhere son song soon sore sort soul sound soup source south space spare spark
speak speaker special species specific speech speed spell spend sphere spice spider spike spill spin spirit spiritual spit spite split spoil spoon
sport spot spray spread spring spy square squeeze stable stack staff stage stain stair stake stall stamp stand standard star stare start
state station statue status stay steady steak steal steam steel steep steer stem step stick still sting stock stomach stone stop store
storm story straight strain strange strategy straw stream street strength stress stretch strict strike string strip stroke strong structure struggle student studio
study stuff stumble stupid style subject subway succeed success such sudden suffer sugar suggest suit summer summit sun super supply support suppose
sure surface surge surgery surprise surround survey survive suspect sustain swallow swamp swan swarm swear sweat sweep sweet swell swift swim swing switch
sword symbol sympathy system table tackle tag tail take tale talent talk tall tank tap tape target task taste tattoo taxi tea teach
teacher team tear tease technical technique technology teen telephone telescope tell temper temperature temple tempo temporary tempt tend tendency tennis tension tent
term terminal terrible territory terror test text thank theater theme then theory there therefore they thick thief thigh thin thing think thirsty
thirty this thorn those though thought thousand thread threat three thrift thrill thrive throat throne through throw thumb thunder thus ticket tide tidy
tie tiger tight tile till tilt time tiny tip tire tired tissue title to toast today toe together toilet token tolerate tomato tomorrow
tone tongue tonight too tool tooth top topic torch toss total touch tough tour tourist toward tower town toy trace track trade
tradition traffic tragic trail train transfer transform translate transport trap trash travel tray treasure treat tree trend trial tribe trick trigger trim
trip trophy tropical trouble truck true trumpet trunk trust truth try tube tuna tune tunnel turkey turn turtle twelve twenty twice twin twist
type typical ugly ultimate unable uncle under undergo undo uniform union unique unit unite universe university unknown unless until unusual upon upper upset
urban urge urgent usage use used useful user usual usually utility utilize utter vacant vacation vaccine vacuum vague valid valley value valve
van vanish vapor variable variety various vary vast vault vector vegetable vehicle vein venture verb verify version very vessel veteran viable victim
victory video view village violate violence virtue virus visible vision visit visual vital vivid vocal voice volume volunteer vote voyage wage wagon
waist wait wake walk wall wander want war ward warm warn wash wasp waste watch water wave way weak wealth weapon wear
weather weave wedding weed week weekend weigh weight weird welcome well west wet whale what whatever wheat wheel when whenever where whether
which while whip whisper white whole whom whose why wide widespread wife wild will win wind window wine wing winner winter wipe wire
wisdom wise wish wit with withdraw within without witness wolf woman wonder wood wooden wool word work worker world worm worry worth would
wound wrap wreck wrestle write writer wrong yard year yellow yes yesterday yet yield yoga young youth zero zone zoo""".split()

import re

WORD_LIST_SET = set(WORD_LIST)
CJK_RE = re.compile(r'[\u4e00-\u9fff]+')
ALPHA_RE = re.compile(r'[A-Za-z]+')
DIGIT_RE = re.compile(r'\d+')
CJK_POOL = 3000        # 常用汉字库大小（攻击者常用字典）
SYMBOL_POOL = 20        # 常见符号池

# 常见弱密码黑名单（rockyou 高频密码，命中即视为秒破）
COMMON_PASSWORDS = {
    "123456","password","12345678","qwerty","123456789","12345","1234","111111",
    "1234567","dragon","123123","baseball","abc123","football","monkey","letmein",
    "696969","shadow","master","666666","qwertyuiop","123321","mustang","1234567890",
    "michael","654321","superman","1qaz2wsx","7777777","121212","000000","qazwsx",
    "123qwe","killer","trustno1","jordan","jennifer","zxcvbnm","asdfgh","hunter",
    "buster","soccer","harley","batman","andrew","tigger","sunshine","iloveyou",
    "charlie","robert","thomas","hockey","ranger","daniel","starwars","112233",
    "george","computer","michelle","jessica","pepper","1111","zxcvbn","555555",
    "11111111","131313","freedom","777777","pass","aaaaaa","ginger","princess",
    "joshua","cheese","amanda","summer","love","ashley","6969","nicole","chelsea",
    "biteme","matthew","access","yankees","987654321","dallas","austin","thunder",
    "taylor","matrix","william","corvette","hello","martin","heather","secret",
    "merlin","diamond","1234qwer","gfhjkm","hammer","silver","222222","88888888",
    "anthony","justin","test","bailey","q1w2e3r4t5","patrick","internet","scooter",
    "orange","11111","golfer","cookie","richard","samantha","bigdog","guitar",
    "jackson","whatever","mickey","chicken","sparky","snoopy","maverick","phoenix",
    "camaro","sexy","peanut","morgan","welcome","falcon","cowboy","ferrari",
    "samsung","andrea","smokey","steelers","gandalf","hardcore","james","carlos",
    "soccer1","rangers","password1","admin","passw0rd","root","toor","test123",
    "qwerty123","pass123","welcome1","monkey123","dragon123","p@ssw0rd","p@ssword",
}

LEET_MAP = str.maketrans({'0':'o','1':'i','3':'e','4':'a','5':'s','7':'t','8':'b','9':'g','2':'z','$':'s','@':'a','!':'i','|':'l'})

def unleet(s):
    """leet 反变换（Tr0ub4dor → troubador 类）"""
    return s.translate(LEET_MAP).lower()

def levenshtein1(a, b):
    """计算编辑距离（限制小字符串，性能足够）"""
    if a == b:
        return 0
    la, lb = len(a), len(b)
    if abs(la - lb) > 1:
        return 2
    prev = list(range(lb + 1))
    for i in range(1, la + 1):
        cur = [i] + [0] * lb
        for j in range(1, lb + 1):
            cur[j] = min(prev[j] + 1, cur[j-1] + 1, prev[j-1] + (a[i-1] != b[j-1]))
        prev = cur
    return prev[lb]

def word_bits(block):
    """字母块的熵：词库命中按词算，否则按 26 字母池"""
    low = block.lower()
    if len(low) >= 3:
        if low in WORD_LIST_SET:
            return math.log2(len(WORD_LIST))
        unl = unleet(low)
        if unl != low and unl in WORD_LIST_SET:
            return math.log2(len(WORD_LIST))
        for w in WORD_LIST:
            if levenshtein1(low, w) <= 1:
                return math.log2(len(WORD_LIST))
    return len(block) * math.log2(26)

def estimate_dict_entropy(password):
    """字典+规则攻击面对的熵：统计可识别词块（含汉字块）"""
    bits = 0.0
    for b in CJK_RE.findall(password):
        bits += len(b) * math.log2(CJK_POOL)
    for b in ALPHA_RE.findall(password):
        low = b.lower()
        if len(low) >= 3:
            if low in WORD_LIST_SET or unleet(low) in WORD_LIST_SET:
                bits += math.log2(len(WORD_LIST))
            elif any(levenshtein1(low, w) <= 1 for w in WORD_LIST):
                bits += math.log2(len(WORD_LIST))
    return bits

# ========================== 核心逻辑 ==========================

def generate_random_password(length=16, use_upper=True, use_lower=True, use_digits=True,
                             use_special=True, avoid_ambiguous=False):
    chars = ""
    if use_lower:  chars += string.ascii_lowercase
    if use_upper:  chars += string.ascii_uppercase
    if use_digits: chars += string.digits
    if use_special: chars += "!@#$%^&*()-_=+[]{}|;:,.<>?"
    if not chars:
        chars = string.ascii_letters + string.digits
    if avoid_ambiguous:
        ambiguous = "0O1lI"
        chars = ''.join(c for c in chars if c not in ambiguous)
        if not chars:
            chars = string.ascii_lowercase.replace('l', '') + string.digits.replace('0', '').replace('1', '')
    return ''.join(secrets.choice(chars) for _ in range(length))


def generate_passphrase(word_count=6, separator="-", include_number=False, title_case=False):
    words = [secrets.choice(WORD_LIST) for _ in range(word_count)]
    if title_case:
        words = [w.capitalize() for w in words]
    if include_number:
        pos = secrets.randbelow(len(words))
        words[pos] = words[pos] + str(secrets.randbelow(100))
    return separator.join(words)


def calculate_entropy(password):
    if not password:
        return 0.0

    # ---- 分块结构熵（识别汉字 / 英文单词 / 数字 / 符号）----
    cjk_blocks = CJK_RE.findall(password)
    alpha_blocks = ALPHA_RE.findall(password)
    digit_blocks = DIGIT_RE.findall(password)

    alpha_word_count = 0
    alpha_bits = 0.0
    for b in alpha_blocks:
        low = b.lower()
        if len(low) >= 3 and (low in WORD_LIST_SET or any(levenshtein1(low, w) <= 1 for w in WORD_LIST)):
            alpha_word_count += 1
            alpha_bits += math.log2(len(WORD_LIST))
        else:
            alpha_bits += len(b) * math.log2(26)

    cjk_bits = sum(len(b) * math.log2(CJK_POOL) for b in cjk_blocks)
    digit_bits = sum(len(b) * math.log2(10) for b in digit_blocks)
    symbol_count = len(password) - sum(len(b) for b in cjk_blocks) \
                   - sum(len(b) for b in alpha_blocks) - sum(len(b) for b in digit_blocks)
    symbol_bits = symbol_count * math.log2(SYMBOL_POOL) if symbol_count > 0 else 0.0

    # 密码含汉字，或包含 >=2 个可识别英文单词，或长度很短的结构混合 → 按结构熵
    if cjk_blocks or alpha_word_count >= 2:
        return round(cjk_bits + alpha_bits + digit_bits + symbol_bits, 2)

    # ---- 纯随机风格密码：按字符集池计算 ----
    has_lower = any(c in string.ascii_lowercase for c in password)
    has_upper = any(c in string.ascii_uppercase for c in password)
    has_digit = any(c in string.digits for c in password)
    has_special = any(c in "!@#$%^&*()-_=+[]{}|;:,.<>?" for c in password)
    has_extended = any(ord(c) > 127 for c in password)

    pool = 0
    if has_lower: pool += 26
    if has_upper: pool += 26
    if has_digit: pool += 10
    if has_special: pool += 22
    if has_extended: pool += 128
    if pool == 0: pool = 1
    return round(len(password) * math.log2(pool), 2)


def estimate_crack_time(entropy, dict_entropy=0.0):
    rates = {
        "online_100ph": 100 / 3600,
        "online_10pm": 10 / 60,
        "cpu_md5": 5e7,
        "cpu_sha256": 1e7,
        "gpu_md5": 2e11,
        "gpu_sha256": 1e10,
        "gpu_bcrypt": 1e5,
        "asic_sha256": 1e13,
    }
    total = 2 ** entropy
    results = {k: total / r / 2 for k, r in rates.items()}
    if dict_entropy > 0:
        # 字典+规则攻击：GPU hashcat 规则攻击约 1e10 次/秒
        results["dict_rule"] = (2 ** dict_entropy) / 1e10 / 2
    return results


def format_duration(seconds):
    if seconds < 1:
        return f"{seconds * 1000:.1f} 毫秒"
    if seconds < 60:
        return f"{seconds:.1f} 秒"
    if seconds < 3600:
        return f"{seconds / 60:.1f} 分钟"
    if seconds < 86400:
        return f"{seconds / 3600:.1f} 小时"
    if seconds < 86400 * 365:
        return f"{seconds / 86400:.1f} 天"
    if seconds < 86400 * 365 * 100:
        return f"{seconds / (86400 * 365):.1f} 年"
    if seconds < 86400 * 365 * 1e6:
        return f"{seconds / (86400 * 365 * 1e3):.1f} 千年"
    if seconds < 86400 * 365 * 1e9:
        return f"{seconds / (86400 * 365 * 1e9):.1f} 十亿年"
    return f"{seconds / (86400 * 365):.2e} 年"


def calculate_hashes(password):
    pw = password.encode('utf-8')
    return {
        "MD5": hashlib.md5(pw).hexdigest(),
        "SHA-1": hashlib.sha1(pw).hexdigest(),
        "SHA-256": hashlib.sha256(pw).hexdigest(),
        "SHA-512": hashlib.sha512(pw).hexdigest(),
        "SHA3-256": hashlib.sha3_256(pw).hexdigest(),
        "Blake2b": hashlib.blake2b(pw).hexdigest(),
    }


def get_strength_label(entropy):
    if entropy < 28:   return "极弱", "#cf3a3a"
    if entropy < 36:   return "弱", "#e67e22"
    if entropy < 60:   return "一般", "#f1c40f"
    if entropy < 80:   return "强", "#2ecc71"
    if entropy < 128:  return "很强", "#27ae60"
    return "极强", "#1abc9c"

# ========================== API 路由 ==========================

@app.route("/")
def index():
    return render_template("index.html")

@app.route("/api/health", methods=["GET"])
def api_health():
    return jsonify({"status": "ok", "version": "4.3"})

@app.route("/api/generate-password", methods=["POST"])
def api_generate_password():
    d = request.get_json() or {}
    length = max(8, min(128, int(d.get("length") or 16)))
    pw = generate_random_password(
        length=length,
        use_upper=d.get("use_upper", True),
        use_lower=d.get("use_lower", True),
        use_digits=d.get("use_digits", True),
        use_special=d.get("use_special", True),
        avoid_ambiguous=d.get("avoid_ambiguous", False),
    )
    entropy = calculate_entropy(pw)
    label, color = get_strength_label(entropy)
    return jsonify({
        "password": pw, "length": length,
        "entropy": entropy, "strength_label": label, "strength_color": color,
    })

@app.route("/api/generate-passphrase", methods=["POST"])
def api_generate_passphrase():
    d = request.get_json() or {}
    count = max(4, min(48, int(d.get("count") or 6)))
    phrases = [generate_passphrase(count, d.get("separator", "-"),
                                    d.get("include_number", False),
                                    d.get("title_case", False)) for _ in range(5)]
    entropy = calculate_entropy(phrases[0])
    label, color = get_strength_label(entropy)
    return jsonify({
        "passphrases": phrases, "word_count": count,
        "entropy": entropy, "strength_label": label, "strength_color": color,
    })

@app.route("/api/analyze", methods=["POST"])
def api_analyze():
    d = request.get_json() or {}
    password = d.get("password", "")
    if not password:
        return jsonify({"error": "密码不能为空"}), 400
    entropy = calculate_entropy(password)
    dict_entropy = estimate_dict_entropy(password)
    # 常见弱密码黑名单（含 leet 变形与尾部数字）：命中直接视为秒破
    raw = password.lower().strip()
    raw_base = re.sub(r'\d+$', '', raw)          # 去尾部数字（保留符号）
    core = re.sub(r'[^a-z0-9]', '', raw)         # 去符号
    base = re.sub(r'\d+$', '', core)             # 去符号+去尾部数字
    unl_raw_base = re.sub(r'[^a-z0-9]', '', unleet(raw_base))  # 先 leet 再去符号
    unl_base = unleet(base)                      # 简化形态再 leet
    common_hit = any(x in COMMON_PASSWORDS for x in
                     (raw, raw_base, core, base, unl_raw_base, unl_base) if x)
    if common_hit:
        entropy = min(entropy, 9.0)
        dict_entropy = min(dict_entropy, 9.0)
    label, color = get_strength_label(entropy)
    crack_times = estimate_crack_time(entropy, dict_entropy)
    return jsonify({
        "password": password, "length": len(password),
        "entropy": entropy, "strength_label": label, "strength_color": color,
        "common_password": common_hit,
        "crack_times": crack_times,
    })

@app.route("/api/hash", methods=["POST"])
def api_hash():
    d = request.get_json() or {}
    password = d.get("password", "")
    if not password:
        return jsonify({"error": "密码不能为空"}), 400
    return jsonify({"password": password, "hashes": calculate_hashes(password)})

# 兼容旧接口（防浏览器缓存旧 JS）
@app.route("/api/generate/random", methods=["POST"])
def api_legacy_random():
    return api_generate_password()

@app.route("/api/generate/passphrase", methods=["POST"])
def api_legacy_passphrase():
    return api_generate_passphrase()

@app.route("/api/crack", methods=["POST"])
def api_legacy_crack():
    return api_analyze()

@app.route("/api/entropy", methods=["POST"])
def api_legacy_entropy():
    d = request.get_json() or {}
    return jsonify({"entropy": calculate_entropy(d.get("password", ""))})

@app.errorhandler(404)
def not_found(e):
    return jsonify({"error": "接口不存在 (404)，请确认正在运行最新版 app.py"}), 404

@app.errorhandler(405)
def method_not_allowed(e):
    return jsonify({"error": "请求方法不允许 (405)"}), 405

# ========================== 启动 ==========================
if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8080))
    host = os.environ.get("HOST", "0.0.0.0")
    print()
    print("=" * 52)
    print("  🔐 密码工坊 多文件版 v4.3 已启动")
    print(f"  手机浏览器打开:  http://127.0.0.1:{port}")
    print("  (局域网设备可用: http://<本机IP>:" + str(port) + ")")
    print("  按 Ctrl+C 停止服务")
    print("=" * 52)
    print()
    app.run(host=host, port=port, debug=False)
