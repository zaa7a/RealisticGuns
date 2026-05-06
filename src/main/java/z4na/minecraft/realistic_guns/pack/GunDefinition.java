package z4na.minecraft.realistic_guns.pack;

/**
 * rlguns/[packname]/guns/xxx.json から読み込んだ銃の定義データ
 */
public class GunDefinition {

    // 銃の識別子 (例: "example_pack:pistol")
    public String id;

    // 表示名
    public String displayName = "Unknown Gun";

    // 最大弾数
    public int maxAmmo = 8;

    // 1発のダメージ
    public float damage = 5.0f;

    // 射程 (ブロック数)
    public float range = 64.0f;

    // 連射速度 (tick単位)
    public int fireRate = 10;

    // リロード時間 (tick)
    public int reloadTime = 40;

    // GeckoLib ジオメトリのパス (例: "example_pack:pistol")
    public String geoModel = "";

    // GeckoLib テクスチャのパス (例: "example_pack:pistol")
    public String texture = "";

    // GeckoLib アニメーションファイルのパス (例: "example_pack:pistol")
    public String animationFile = "";

    // アニメーション名の定義
    public AnimationNames animations = new AnimationNames();

    public static class AnimationNames {
        public String idle    = "animation.gun.idle";
        public String shoot   = "animation.gun.shoot";
        public String reload  = "animation.gun.reload";
        // 残弾確認モーション
        public String inspect = "animation.gun.inspect";
    }

    @Override
    public String toString() {
        return "GunDefinition{id='" + id + "', displayName='" + displayName + "', maxAmmo=" + maxAmmo + "}";
    }
}