package model;

/**
 * Возможные действия хоккеиста.
 * <p/>
 * Хоккеист может совершить действие, если он не сбит с ног ({@code HockeyistState.KNOCKED_DOWN}),
 * не отдыхает ({@code HockeyistState.RESTING}) и уже восстановился после своего предыдущего действия
 * (значение {@code hockeyist.remainingCooldownTicks} равно {@code 0}).
 * <p/>
 * Если хоккеист замахивается клюшкой ({@code HockeyistState.SWINGING}), то из действий ему доступны только
 * {@code ActionType.STRIKE} и {@code ActionType.CANCEL_STRIKE}.
 */
public enum ActionType {
    /**
     * Ничего не делать.
     */
    NONE,

    /**
     * Взять шайбу.
     * <p/>
     * Если хоккеист уже контролирует шайбу, либо шайба находится вне зоны досягаемости клюшки хоккеиста (смотрите
     * документацию к значениям {@code game.stickLength} и {@code game.stickSector}), то действие игнорируется.
     * <p/>
     * В противном случае хоккеист попытается установить контроль над шайбой и, с определённой вероятностью,
     * это сделает ((смотрите документацию к {@code game.pickUpPuckBaseChance} и {@code game.takePuckAwayBaseChance})).
     */
    TAKE_PUCK,

    /**
     * Замахнуться для удара.
     * <p/>
     * Хоккеист замахивается для увеличения силы удара. Чем больше тиков пройдёт с момента начала замаха до удара,
     * тем большее воздействие будет на попавшие под удар объекты. Максимальное количество учитываемых тиков ограничено
     * значением {@code game.maxEffectiveSwingTicks}.
     */
    SWING,

    /**
     * Ударить.
     * <p/>
     * Хоккеист наносит размашистый удар по всем объектам, находящимся в зоне досягаемости его клюшки. Удар может быть
     * совершён как с предварительным замахом ({@code SWING}), так и без него (в этом случае сила удара будет меньше).
     * <p/>
     * Объекты (шайба и хоккеисты, кроме вратарей), попавшие под удар, приобретут некоторый импульс в направлении,
     * совпадающим с направлением удара. При ударе по хоккеисту есть также некоторый шанс сбить его с ног.
     */
    STRIKE,

    /**
     * Отменить удар.
     * <p/>
     * Хоккеист выходит из состояния замаха ({@code SWING}), не совершая удар. Это позволяет
     * сэкономить немного выносливости, а также быстрее совершить новое действие
     * (смотрите документацию к {@code game.cancelStrikeActionCooldownTicks}).
     * <p/>
     * Если хоккеист не совершает замах клюшкой, то действие игнорируется.
     */
    CANCEL_STRIKE,

    /**
     * Отдать пас.
     * <p/>
     * Хоккеист пытается передать контролируемую им шайбу другому хоккеисту. Для этого необходимо указать относительную
     * силу паса ({@code move.passPower}) и его направление ({@code move.passAngle}). В противном случае пас будет отдан
     * в направлении, соответствующем направлению хоккеиста, с максимально возможной силой.
     * <p/>
     * Если хоккеист не контролирует шайбу, то действие игнорируется.
     */
    PASS,

    /**
     * Заменить активного хоккеиста сидящим на скамейке запасных.
     * <p/>
     * Замена выполняется только на своей половине поля, при этом расстояние от центра хоккеиста до верхней границы
     * игровой площадки не должно превышать {@code game.substitutionAreaHeight}. Дополнительно нужно указать индекс
     * хоккеиста ({@code move.teammateIndex}), на которого будет произведена замена.
     * <p/>
     * Если указан некорректный индекс, или скорость хоккеиста превышает {@code game.maxSpeedToAllowSubstitute},
     * то действие будет проигнорировано.
     */
    SUBSTITUTE
}
