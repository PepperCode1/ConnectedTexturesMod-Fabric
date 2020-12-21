package team.chisel.ctm.client.resource;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

public class BlockstatePredicateParser {
	private static final Type MAP_TYPE = new TypeToken<EnumMap<Direction, Predicate<BlockState>>>() {}.getType();
	private static final Type PREDICATE_TYPE = new TypeToken<Predicate<BlockState>>() {}.getType();
	private final PredicateDeserializer predicateDeserializer = new PredicateDeserializer();
	private final Gson GSON = new GsonBuilder().registerTypeAdapter(PREDICATE_TYPE, predicateDeserializer).registerTypeAdapter(ComparisonType.class, new ComparisonType.Deserializer()).registerTypeAdapter(MAP_TYPE, (InstanceCreator<?>) type -> new EnumMap<>(Direction.class)).registerTypeAdapter(PredicateMap.class, new MapDeserializer()).create();

	@Nullable
	public BiPredicate<Direction, BlockState> parse(JsonElement json) {
		return GSON.fromJson(json, PredicateMap.class);
	}
	
	private static enum ComparisonType {
		EQUAL("=", i -> i == 0), NOT_EQUAL("!=", i -> i != 0), GREATER_THAN(">", i -> i > 0), LESS_THAN("<", i -> i < 0), GREATER_THAN_EQ(">=", i -> i >= 0), LESS_THAN_EQ("<=", i -> i <= 0);
		private final String key;
		private final IntPredicate compareFunc;

		static class Deserializer implements JsonDeserializer<ComparisonType> {
			@Override
			public ComparisonType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
					Optional<ComparisonType> type = Arrays.stream(ComparisonType.values()).filter(t -> t.key.equals(json.getAsString())).findFirst();
					if (type.isPresent()) {
						return type.get();
					}
					throw new JsonParseException(json + " is not a valid comparison type!");
				}
				throw new JsonSyntaxException("ComparisonType must be a String");
			}
		}

		private ComparisonType(final String key, final IntPredicate compareFunc) {
			this.key = key;
			this.compareFunc = compareFunc;
		}
	}

	private static enum Composition {
		AND(Predicate::and), OR(Predicate::or);
		private final BiFunction<Predicate<BlockState>, Predicate<BlockState>, Predicate<BlockState>> composer;

		private Composition(final BiFunction<Predicate<BlockState>, Predicate<BlockState>, Predicate<BlockState>> composer) {
			this.composer = composer;
		}
	}

	private static final class PropertyPredicate<T extends Comparable<T>> implements Predicate<BlockState> {
		private final Block block;
		private final Property<T> prop;
		private final T value;
		private final ComparisonType type;

		public PropertyPredicate(final Block block, final Property<T> prop, final T value, final ComparisonType type) {
			this.block = block;
			this.prop = prop;
			this.value = value;
			this.type = type;
		}

		@Override
		public boolean test(BlockState t) {
			return t.getBlock() == block && type.compareFunc.test(t.get(prop).compareTo(value));
		}

		public Block getBlock() {
			return this.block;
		}

		public Property<T> getProp() {
			return this.prop;
		}

		public T getValue() {
			return this.value;
		}

		public ComparisonType getType() {
			return this.type;
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) return true;
			if (!(o instanceof BlockstatePredicateParser.PropertyPredicate)) return false;
			final BlockstatePredicateParser.PropertyPredicate<?> other = (BlockstatePredicateParser.PropertyPredicate<?>) o;
			final Object this$block = this.getBlock();
			final Object other$block = other.getBlock();
			if (this$block == null ? other$block != null : !this$block.equals(other$block)) return false;
			final Object this$prop = this.getProp();
			final Object other$prop = other.getProp();
			if (this$prop == null ? other$prop != null : !this$prop.equals(other$prop)) return false;
			final Object this$value = this.getValue();
			final Object other$value = other.getValue();
			if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
			final Object this$type = this.getType();
			final Object other$type = other.getType();
			if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			final Object $block = this.getBlock();
			result = result * PRIME + ($block == null ? 43 : $block.hashCode());
			final Object $prop = this.getProp();
			result = result * PRIME + ($prop == null ? 43 : $prop.hashCode());
			final Object $value = this.getValue();
			result = result * PRIME + ($value == null ? 43 : $value.hashCode());
			final Object $type = this.getType();
			result = result * PRIME + ($type == null ? 43 : $type.hashCode());
			return result;
		}

		@Override
		public String toString() {
			return "BlockstatePredicateParser.PropertyPredicate(block=" + this.getBlock() + ", prop=" + this.getProp() + ", value=" + this.getValue() + ", type=" + this.getType() + ")";
		}
	}

	private static final class MultiPropertyPredicate<T extends Comparable<T>> implements Predicate<BlockState> {
		private final Block block;
		private final Property<T> prop;
		private final Set<T> validValues;

		@Override
		public boolean test(BlockState t) {
			return t.getBlock() == block && validValues.contains(t.get(prop));
		}

		@SuppressWarnings("all")
		public MultiPropertyPredicate(final Block block, final Property<T> prop, final Set<T> validValues) {
			this.block = block;
			this.prop = prop;
			this.validValues = validValues;
		}

		@SuppressWarnings("all")
		public Block getBlock() {
			return this.block;
		}

		@SuppressWarnings("all")
		public Property<T> getProp() {
			return this.prop;
		}

		@SuppressWarnings("all")
		public Set<T> getValidValues() {
			return this.validValues;
		}

		@Override
		@SuppressWarnings("all")
		public boolean equals(final Object o) {
			if (o == this) return true;
			if (!(o instanceof BlockstatePredicateParser.MultiPropertyPredicate)) return false;
			final BlockstatePredicateParser.MultiPropertyPredicate<?> other = (BlockstatePredicateParser.MultiPropertyPredicate<?>) o;
			final Object this$block = this.getBlock();
			final Object other$block = other.getBlock();
			if (this$block == null ? other$block != null : !this$block.equals(other$block)) return false;
			final Object this$prop = this.getProp();
			final Object other$prop = other.getProp();
			if (this$prop == null ? other$prop != null : !this$prop.equals(other$prop)) return false;
			final Object this$validValues = this.getValidValues();
			final Object other$validValues = other.getValidValues();
			if (this$validValues == null ? other$validValues != null : !this$validValues.equals(other$validValues)) return false;
			return true;
		}

		@Override
		@SuppressWarnings("all")
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			final Object $block = this.getBlock();
			result = result * PRIME + ($block == null ? 43 : $block.hashCode());
			final Object $prop = this.getProp();
			result = result * PRIME + ($prop == null ? 43 : $prop.hashCode());
			final Object $validValues = this.getValidValues();
			result = result * PRIME + ($validValues == null ? 43 : $validValues.hashCode());
			return result;
		}

		@Override
		@SuppressWarnings("all")
		public String toString() {
			return "BlockstatePredicateParser.MultiPropertyPredicate(block=" + this.getBlock() + ", prop=" + this.getProp() + ", validValues=" + this.getValidValues() + ")";
		}
	}

	private static final class BlockPredicate implements Predicate<BlockState> {
		private final Block block;

		public BlockPredicate(final Block block) {
			this.block = block;
		}

		@Override
		public boolean test(BlockState t) {
			return t.getBlock() == block;
		}

		public Block getBlock() {
			return this.block;
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) return true;
			if (!(o instanceof BlockstatePredicateParser.BlockPredicate)) return false;
			final BlockstatePredicateParser.BlockPredicate other = (BlockstatePredicateParser.BlockPredicate) o;
			final Object this$block = this.getBlock();
			final Object other$block = other.getBlock();
			if (this$block == null ? other$block != null : !this$block.equals(other$block)) return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			final Object $block = this.getBlock();
			result = result * PRIME + ($block == null ? 43 : $block.hashCode());
			return result;
		}

		@Override
		public String toString() {
			return "BlockstatePredicateParser.BlockPredicate(block=" + this.getBlock() + ")";
		}
	}

	private static class PredicateComposition implements Predicate<BlockState> {
		private final Composition type;
		private final List<Predicate<BlockState>> composed;

		public PredicateComposition(final Composition type, final List<Predicate<BlockState>> composed) {
			this.type = type;
			this.composed = composed;
		}

		@Override
		public boolean test(BlockState t) {
			if (type == Composition.AND) {
				for (Predicate<BlockState> p : composed) {
					if (!p.test(t)) {
						return false;
					}
				}
				return true;
			} else {
				for (Predicate<BlockState> p : composed) {
					if (p.test(t)) {
						return true;
					}
				}
				return false;
			}
		}

		@Override
		public String toString() {
			return "BlockstatePredicateParser.PredicateComposition(type=" + this.type + ", composed=" + this.composed + ")";
		}
	}

	private static class PredicateDeserializer implements JsonDeserializer<Predicate<BlockState>> {
		final Predicate<BlockState> EMPTY = p -> false;
		// Unlikely that this will be threaded, but I think foamfix tries, so let's be safe
		// A global cache for the default predicate for use in creating deferring predicates
		ThreadLocal<Predicate<BlockState>> defaultPredicate = new ThreadLocal<>();

		@Override
		public Predicate<BlockState> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonObject()) {
				JsonObject obj = json.getAsJsonObject();
				Block block = Registry.BLOCK.get(new Identifier(JsonHelper.getString(obj, "block")));
				if (block == Blocks.AIR) {
					return EMPTY;
				}
				Composition composition = null;
				if (obj.has("defer")) {
					if (defaultPredicate.get() == null) {
						throw new JsonParseException("Cannot defer when no default is set!");
					}
					try {
						composition = Composition.valueOf(JsonHelper.getString(obj, "defer").toUpperCase(Locale.ROOT));
					} catch (IllegalArgumentException e) {
						throw new JsonSyntaxException(JsonHelper.getString(obj, "defer") + " is not a valid defer type.");
					}
				}
				if (!obj.has("predicate")) {
					return compose(composition, new BlockPredicate(block));
				}
				JsonElement propsEle = obj.get("predicate");
				if (propsEle.isJsonObject()) {
					return compose(composition, parsePredicate(block, propsEle.getAsJsonObject(), context));
				} else if (propsEle.isJsonArray()) {
					List<Predicate<BlockState>> predicates = new ArrayList<>();
					for (JsonElement ele : propsEle.getAsJsonArray()) {
						if (ele.isJsonObject()) {
							predicates.add(parsePredicate(block, ele.getAsJsonObject(), context));
						} else {
							throw new JsonSyntaxException("Predicate entry must be a JSON Object. Found: " + ele);
						}
					}
					return compose(composition, new PredicateComposition(Composition.AND, predicates));
				}
			} else if (json.isJsonArray()) {
				List<Predicate<BlockState>> predicates = new ArrayList<>();
				for (JsonElement ele : json.getAsJsonArray()) {
					Predicate<BlockState> p = context.deserialize(ele, PREDICATE_TYPE);
					if (p != EMPTY) {
						predicates.add(p);
					}
				}
				return predicates.size() == 0 ? EMPTY : predicates.size() == 1 ? predicates.get(0) : new PredicateComposition(Composition.OR, predicates);
			}
			throw new JsonSyntaxException("Predicate deserialization expects an object or an array. Found: " + json);
		}

		private Predicate<BlockState> compose(@Nullable Composition composition, @NotNull Predicate<BlockState> child) {
			if (composition == null) {
				return child;
			}
			return composition.composer.apply(defaultPredicate.get(), child);
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		private Predicate<BlockState> parsePredicate(@NotNull Block block, JsonObject obj, JsonDeserializationContext context) {
			ComparisonType compareFunc = JsonHelper.deserialize(obj, "compare_func", ComparisonType.EQUAL, context, ComparisonType.class);
			obj.remove("compare_func");
			final Set<Map.Entry<String, JsonElement>> entryset = obj.entrySet();
			if (entryset.size() > 1 || entryset.size() == 0) {
				throw new JsonSyntaxException("Predicate entry must define exactly one property->value pair. Found: " + entryset.size());
			}
			String key = entryset.iterator().next().getKey();
			Optional<Property<?>> prop = block.getStateManager().getProperties().stream().filter(p -> p.getName().equals(key)).findFirst();
			if (!prop.isPresent()) {
				throw new JsonParseException(key + " is not a valid property for blockstate " + block.getDefaultState());
			}
			JsonElement valueEle = obj.get(key);
			if (valueEle.isJsonArray()) {
				return new MultiPropertyPredicate(block, prop.get(), StreamSupport.stream(valueEle.getAsJsonArray().spliterator(), false).map(e -> this.parseValue(prop.get(), e)).collect(Collectors.toSet()));
			} else {
				return new PropertyPredicate(block, prop.get(), parseValue(prop.get(), valueEle), compareFunc);
			}
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		private Comparable parseValue(Property prop, JsonElement ele) {
			String valstr = JsonHelper.asString(ele, prop.getName());
			Optional<Comparable> value = (Optional<Comparable>) prop.getValues().stream().filter(v -> prop.name((Comparable) v).equalsIgnoreCase(valstr)).findFirst();
			if (!value.isPresent()) {
				throw new JsonParseException(valstr + " is not a valid value for property " + prop);
			}
			return value.get();
		}
	}

	private static class PredicateMap implements BiPredicate<Direction, BlockState> {
		private final EnumMap<Direction, Predicate<BlockState>> predicates = new EnumMap<>(Direction.class);

		@Override
		public boolean test(Direction dir, BlockState state) {
			return predicates.get(dir).test(state);
		}
	}

	private class MapDeserializer implements JsonDeserializer<PredicateMap> {
		@Override
		public PredicateMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonObject()) {
				JsonObject obj = json.getAsJsonObject();
				if (obj.has("default")) {
					predicateDeserializer.defaultPredicate.set(context.deserialize(obj.get("default"), PREDICATE_TYPE));
					obj.remove("default");
				}
				PredicateMap ret = new PredicateMap();
				ret.predicates.putAll(context.deserialize(obj, MAP_TYPE));
				for (Direction dir : Direction.values()) {
					ret.predicates.putIfAbsent(dir, Optional.ofNullable(predicateDeserializer.defaultPredicate.get()).orElse(predicateDeserializer.EMPTY));
				}
				predicateDeserializer.defaultPredicate.set(null);
				return ret;
			} else if (json.isJsonArray()) {
				Predicate<BlockState> predicate = context.deserialize(json, PREDICATE_TYPE);
				PredicateMap ret = new PredicateMap();
				for (Direction dir : Direction.values()) {
					ret.predicates.put(dir, predicate);
				}
				return ret;
			}
			throw new JsonSyntaxException("connectTo must be an object or an array. Found: " + json);
		}
	}
}
