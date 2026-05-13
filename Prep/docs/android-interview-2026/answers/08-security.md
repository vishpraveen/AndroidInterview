# Answers — §8 Security

Questions: [questions.md](../questions.md#8-security).

---

### Network Security

**1.** SSL pinning: certificate vs public key pinning.

- [x]

- **Thesis:** **Certificate pinning** locks exact leaf/spool cert (brittle to rotation). **SPKI pinning** hashes **public key** (survives reissue with same key pair)—**recommended** for rotation tolerance with backup pins.
- **Mechanism / order:** OkHttp `CertificatePinner` with `sha256/AA==` pins + **backup pins**.
- **Edge cases:** **CDN** cert changes; corporate MITM proxies break legit users.
- **Production hook:** **Network security config** debug overrides.
- **When wrong:** Pinning without **rotation playbook**—outage.

**2.** OkHttp `CertificatePinner`; rotation.

- [x]

- **Thesis:** `CertificatePinner.Builder.add("host", "sha256/...")` fails handshake if chain not match; ship **multiple pins** + **remote config** to add new before old expires.
- **Mechanism / order:** `ChainCleaner` respects platform trust store unless overridden.
- **Edge cases:** **Certificate transparency** logs help detect mis-issuance not pinning itself.
- **Production hook:** Staged rollout of pin updates separate from app binary if using dynamic trust (careful—still risky).
- **When wrong:** Single pin, no backup—brick app at midnight cert renewal.

**3.** `network_security_config.xml`.

- [x]

- **Thesis:** Declares **custom trust anchors**, **debug-only** user CAs, **cleartext** opt-in per domain, **pin-set** integration variants historically—manifest `android:networkSecurityConfig` reference.
- **Mechanism / order:** `<domain-config cleartextTrafficPermitted="false">` per host.
- **Edge cases:** **Debug** `<debug-overrides>` must not ship permissive config to prod merge mistakes.
- **Production hook:** `res/xml/network_security_config.xml` per flavor.
- **When wrong:** `base-config cleartextTrafficPermitted=true` globally.

**4.** Prevent MITM.

- [x]

- **Thesis:** TLS everywhere, **pinning** for high-risk, **no user-added CAs** in prod, **HSTS** on server, **hostname verification** default OkHttp, **certificate transparency** monitoring, detect **proxy** settings tampering where feasible.
- **Mechanism / order:** Avoid custom `TrustManager` accepting all.
- **Edge cases:** **Split tunnel VPN** user support burden vs security.
- **Production hook:** Server **OCSP stapling**.
- **When wrong:** `TrustAllCerts` in “debug only” flag left enabled.

**5.** Tools bypassing pinning (Frida/Magisk/Xposed); detect/counter.

- [x]

- **Thesis:** Attackers hook **SSLContext** or OkHttp **CertificatePinner.check**—**no** perfect client-side defense; combine **Play Integrity**, **runtime integrity checks** (heuristic), **server-side** risk scoring, **obfuscation** to raise cost only.
- **Mechanism / order:** Detect **debuggable**, **su**, **hook frameworks** signals—**false positives** harm users.
- **Edge cases:** Enterprise devices with MDM proxies.
- **Production hook:** Don’t rely solely on pinning for auth—**tokens** short-lived + server revocation.
- **When wrong:** Aggressive hard block on rooted devices losing legitimate power users.

**6.** Certificate transparency (CT).

- [x]

- **Thesis:** Public **logs** of all issued certs allow monitors to detect **rogue CAs** quickly; apps indirectly benefit via **CT policy** enforced by modern Android network stack expectations + server configuration.
- **Mechanism / order:** Not a replacement for pinning—**defense in depth** at PKI ecosystem layer.
- **Edge cases:** **Private PKI** internal apps differ.
- **Production hook:** Server enables **CT** for public certs.
- **When wrong:** Expecting Android app alone to “enable CT” without server logs.

---

### Data Storage Security

**7.** `EncryptedSharedPreferences`.

- [x]

- **Thesis:** Wraps keys/values with **AES-GCM** using keys from **Android Keystore** (`MasterKey` AES256 scheme); prevents trivial **file** scraping on rooted devices but not **runtime memory** hooking.
- **Mechanism / order:** `EncryptedSharedPreferences.create(...)`.
- **Edge cases:** **Key invalidation** on biometric enrollment changes if bound keys.
- **Production hook:** Prefer **DataStore** + encryption or **SQLCipher** for structured secrets volume.
- **When wrong:** Thinking encryption stops **root** file read of decrypted memory.

**8.** Android Keystore; key types; hardware-backed guarantees.

- [x]

- **Thesis:** TEE/StrongBox-backed **key material not extractable**; supports **RSA/EC/AES** keys with **user authentication** binding, **attestation** chains.
- **Mechanism / order:** `KeyGenParameterSpec` sets purposes, digests, **setUserAuthenticationRequired**.
- **Edge cases:** **StrongBox** optional hardware feature—fallback path testing.
- **Production hook:** Use for **Wrapping** local data encryption keys (envelope encryption).
- **When wrong:** Storing raw AES key in `SharedPreferences` next to ciphertext.

**9.** Encrypt Room; SQLCipher; performance.

- [x]

- **Thesis:** **SQLCipher** replaces SQLite `open` with **key derivation** (PBKDF2) + page encryption; Room via **`SupportFactory`** passphrase; **CPU** overhead on read/write + larger pages.
- **Mechanism / order:** Cache **derived key** carefully—still protect with Keystore-wrapped secret.
- **Edge cases:** **ATTACH DATABASE** limitations; **WAL** still works with cipher.
- **Production hook:** Benchmark cold query paths; use **hardware** Keystore for passphrase unwrap only occasionally.
- **When wrong:** Hard-coded passphrase string in APK.

**10.** Hardware-backed vs software Keystore.

- [x]

- **Thesis:** **Hardware-backed** keys never leave secure element/TEE; **software** fallback stores wrapped blobs in app storage—**weaker** to offline brute force if attacker extracts files + knows device unlock sometimes.
- **Mechanism / order:** `Security.getProviders` / `KeyInfo.isInsideSecureHardware()`.
- **Edge cases:** Emulator software-only.
- **Production hook:** Feature detect before requiring StrongBox-only algorithms.
- **When wrong:** Assuming all devices StrongBox.

**11.** Secure API keys: BuildConfig vs NDK vs remote.

- [x]

- **Thesis:** **Anything in APK is attacker-readable**; **public client keys** (Firebase API keys) rely on **server rules** + **App Check** not secrecy. **Secrets** belong on **server** or **short-lived tokens** post-auth.
- **Mechanism / order:** NDK string splitting only **obscures**; **remote config** still delivered to client.
- **Edge cases:** **OAuth confidential** clients cannot hide secret in mobile public client—use **PKCE**.
- **Production hook:** **Play Integrity** + backend issuance.
- **When wrong:** Admin service account JSON in assets.

**12.** `BiometricPrompt` + Keystore crypto.

- [x]

- **Thesis:** Prompt gates **CryptoObject** (`Cipher`/`Signature`) whose key requires **recent auth**; successful auth starts crypto operation unlocking local data.
- **Mechanism / order:** `setUserAuthenticationParameters(timeout, types)` API levels differ.
- **Edge cases:** **Class 3** vs **Class 2** sensors; fallback to device credential.
- **Production hook:** Use **BiometricPrompt** not deprecated `FingerprintManager`.
- **When wrong:** Storing “biometric hash” locally—use platform crypto paths.

---

### Code and App Security

**13.** Obfuscation insufficient alone.

- [x]

- **Thesis:** Obfuscation is **obscurity**—doesn’t stop determined reverse engineers; combine **server validation**, **integrity checks**, **rate limits**, **token binding**.
- **Mechanism / order:** R8 still leaves **control flow** analyzable with effort.
- **Edge cases:** **Reflection metadata** leaks names if kept.
- **Production hook:** Threat modeling **STRIDE** per feature.
- **When wrong:** “We’re safe—we obfuscate.”

**14.** Root detection; limitations.

- [x]

- **Thesis:** Heuristics: **`su` binaries**, **Magisk** mounts, **SafetyNet/Play Integrity** verdicts, **debugger** attached; **false positives/negatives** inevitable; **privacy** concerns blocking legit users.
- **Mechanism / order:** Risk-based degrade features not hard crash always.
- **Edge cases:** **Work profiles**, **emulators** for dev.
- **Production hook:** **Play Integrity STANDARD** device verdict.
- **When wrong:** Hard ban excludes developers and some markets.

**15.** SafetyNet → Play Integrity API.

- [x]

- **Thesis:** **Attestation** proves genuine device + unmodified app binary signing cert + Google Play licensing signals; server verifies **JWT** from Play Integrity API.
- **Mechanism / order:** Nonce from server prevents replay.
- **Edge cases:** **Offline** verification impossible fully client-side.
- **Production hook:** Backend stores Google public keys rotating.
- **When wrong:** Trusting client-side boolean only.

**16.** RASP examples on Android.

- [x]

- **Thesis:** **Runtime Application Self-Protection**: hook detection, **debugger/tamper** checks, **emulator** detection, **checksum** self-integrity, **obfuscated** control flow, **anti-screenshot** for sensitive screens.
- **Mechanism / order:** Balance UX vs **false positives**.
- **Edge cases:** Play policy on **deceptive behavior** if hiding malware-like patterns—stay transparent in privacy policy.
- **Production hook:** Layer with server risk scoring.
- **When wrong:** RASP as only line ignoring server authorization.

**17.** Repackaging / tampering; signature verification.

- [x]

- **Thesis:** Play **signing** v2/v3 ensures updates same signer; app can compare **`PackageManager.getPackageInfo.signatures`** to expected for **defense in depth** (rare need). **Tamper**: verify **DEX** checksums (fragile, maintenance heavy).
- **Mechanism / order:** **Play Integrity** better modern signal.
- **Edge cases:** **Dynamic delivery** modules signed too.
- **Production hook:** License verification server-side with purchase token.
- **When wrong:** DIY signature check that breaks on Play App Signing key rotation if misunderstood.

**18.** `StrictMode` for security in dev.

- [x]

- **Thesis:** Detect **leaked** closables, **disk/network on main**, **cleartext** traffic policy violations via thread policy—mostly **quality** but catches accidental **world-readable** file APIs misuse indirectly via patterns.
- **Mechanism / order:** `VmPolicy` with `detectFileUriExposure`.
- **Edge cases:** No-op in release unless you explicitly enable (don’t).
- **Production hook:** `penaltyDeath` only in debug builds.
- **When wrong:** StrictMode in prod crashing users.

---

### OWASP Mobile Top 10 (condensed walk)

**19.** OWASP Mobile Top 10 — risk + Android mitigation (ultra-brief per item).

- [x]

- **Thesis:** Typical list themes: **M1 improper platform usage** → follow intents/exported components rules; **M2 insecure data storage** → Keystore, SQLCipher, no logs of PII; **M3 insecure comms** → TLS+pinning; **M4 insecure auth** → BiometricPrompt, token refresh; **M5 insufficient cryptography** → modern AES-GCM, avoid custom; **M6 insecure authorization** → server checks role every call; **M7 poor code quality** → lint, tests; **M8 code tampering** → Play Integrity; **M9 reverse engineering** → R8 + server truth; **M10 extraneous functionality** → remove debug endpoints.
- **Mechanism / order:** Map each feature to **controls** in design review.
- **Edge cases:** List revisions (2016 vs newer) numbering shifts—name risks not just numbers in interview.
- **Production hook:** OWASP **MASVS** baseline self-assessment.
- **When wrong:** Checkbox compliance without threat model.

**20.** Sensitive data in recents screenshot — `FLAG_SECURE`.

- [x]

- **Thesis:** `WindowManager.LayoutParams.FLAG_SECURE` prevents **screenshots** and **recent tasks thumbnail** for that window.
- **Mechanism / order:** `window.setFlags(FLAG_SECURE, FLAG_SECURE)` on sensitive activities.
- **Edge cases:** **MediaProjection** still user-consented—different path.
- **Production hook:** Banking/video DRM patterns.
- **When wrong:** Forgetting **Compose** dialog windows separate flags.

**21.** WebView security.

- [x]

- **Thesis:** Disable **`javascriptInterface`** unless needed; **`@JavascriptInterface`** exposed only on **HTTPS** trusted origins; **`shouldOverrideUrlLoading`** block unknown schemes; **`setAllowFileAccess`** false; update **WebView** provider; **CSP** on HTML.
- **Mechanism / order:** **`addJavascriptInterface`** is **attack surface** for URL hijacking.
- **Edge cases:** **`setMixedContentMode`** never allow mixed in prod.
- **Production hook:** Host app-controlled HTML or sanitize.
- **When wrong:** `webView.loadUrl(intent.data)` from deep link without validation.

**22.** Intent redirection; exported components.

- [x]

- **Thesis:** Malicious app sends intent to your **exported** activity that forwards inner intent to **private** component/privileged action—**confused deputy**. Fix: **reconcile intents**, validate **package/component**, strip extras, use **explicit** intents, **`android:permission`** on receivers, **`signature` protection level** for internal broadcasts.
- **Mechanism / order:** PendingIntent **immutable** + fill-in extras control.
- **Edge cases:** **`SELECT`** chooser attacks.
- **Production hook:** Pen test exported surface.
- **When wrong:** `startActivity(getIntent())` in exported proxy.

---

### Authentication and Authorization

**23.** Secure token storage + refresh.

- [x]

- **Thesis:** **Access token** short-lived in **encrypted** storage; **refresh** via HTTPS only; **rotate refresh tokens** server-side; detect **reuse** replay attack; clear on logout; bind to **Play Integrity** optional.
- **Mechanism / order:** OkHttp **Authenticator** for 401 refresh single-flight mutex.
- **Edge cases:** **Process death** mid-refresh—persist refresh state carefully.
- **Production hook:** Use **BiometricPrompt** to unlock keystore key wrapping refresh token.
- **When wrong:** Infinite refresh loop on bad clock—handle **skew**.

**24.** OAuth2 + PKCE for mobile.

- [x]

- **Thesis:** **Public clients** cannot hold client secret—**PKCE** (`code_verifier`/`code_challenge` S256) prevents auth code interception by malicious app registering same URL scheme or MITM proxying redirect.
- **Mechanism / order:** Custom tabs **Trusted Web Activity** preferred over WebView for auth where possible.
- **Edge cases:** **Redirect URI** exact match allowlist server-side.
- **Production hook:** Use mature SDK (AppAuth).
- **When wrong:** Implicit flow with access token in redirect URI fragment.

**25.** Session management: timeout, re-auth, biometric gate.

- [x]

- **Thesis:** **Idle timeout** clears sensitive state; **step-up** auth for high-risk actions; **OS-level** biometric gate before showing PII; **server** invalidates all sessions on password change.
- **Mechanism / order:** `BiometricPrompt` + `KeyPermanentlyInvalidatedException` handle enrollment changes.
- **Edge cases:** **Multi-device** session list UX.
- **Production hook:** `WorkManager` periodic silent refresh only when safe.
- **When wrong:** “Remember me” forever token with no server revocation.
