package team.chisel.ctm.client.texture.type;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.render.CTMLogic;
import team.chisel.ctm.client.texture.TextureEdges;
import team.chisel.ctm.client.texture.context.TextureContextCTM;
import team.chisel.ctm.client.util.ConnectionDirection;

public class TextureTypeEdges extends TextureTypeCTM {
	@Override
	public CTMTexture<? extends TextureTypeCTM> makeTexture(TextureInfo info) {
		return new TextureEdges(this, info);
	}

	@Override
	public TextureContextCTM getTextureContext(BlockState state, BlockView world, BlockPos pos, CTMTexture<?> texture) {
		return new TextureContextCTM(state, world, pos, (TextureEdges) texture) {
			@Override
			protected CTMLogic createCTM(BlockState state) {
				CTMLogic parent = super.createCTM(state);
				// FIXME
				CTMLogic logic = new CTMLogicEdges();
				logic.ignoreStates(parent.ignoreStates()).stateComparator(parent.stateComparator());
				logic.disableObscuredFaceCheck = parent.disableObscuredFaceCheck;
				return logic;
			}
		};
	}

	@Override
	public int requiredTextures() {
		return 3;
	}

	public static class CTMLogicEdges extends CTMLogic {
		private boolean obscured;

		public void setObscured(final boolean obscured) {
			this.obscured = obscured;
		}

		public boolean isObscured() {
			return obscured;
		}

		@Override
		public boolean isConnected(BlockView world, BlockPos current, BlockPos connection, Direction direction, BlockState state) {
			if (isObscured()) {
				return false;
			}
			BlockState obscuring = getConnectionState(world, current.offset(direction), direction, current);
			if (stateComparator(state, obscuring, direction)) {
				setObscured(true);
				return false;
			}
			BlockState connectionState = getConnectionState(world, connection, direction, current);
			BlockState obscuringcon = getConnectionState(world, connection.offset(direction), direction, current);
			if (stateComparator(state, connectionState, direction) || stateComparator(state, obscuringcon, direction)) {
				Vec3d difference = Vec3d.of(connection.subtract(current));
				if (difference.lengthSquared() > 1) {
					difference = difference.normalize();
					if (direction.getAxis() == Axis.Z) {
						difference = difference.rotateY((float) (-Math.PI / 2));
					}
					float angle = (float) Math.PI / 4;
					Vec3d vA;
					Vec3d vB;
					if (direction.getAxis().isVertical()) {
						vA = difference.rotateY(angle);
						vB = difference.rotateY(-angle);
					} else {
						vA = difference.rotateX(angle);
						vB = difference.rotateX(-angle);
					}
					BlockPos posA = new BlockPos(vA).add(current);
					BlockPos posB = new BlockPos(vB).add(current);
					return (getConnectionState(world, posA, direction, current) == state && !stateComparator(state, getConnectionState(world, posA.offset(direction), direction, current), direction)) || (getConnectionState(world, posB, direction, current) == state && !stateComparator(state, getConnectionState(world, posB.offset(direction), direction, current), direction));
				} else {
					return true;
				}
			}
			return false;
		}

		@Override
		protected void fillSubmaps(int index) {
			ConnectionDirection[] directions = SUBMAP_MAP[index];
			if (!connectedOr(directions[0], directions[1]) && connected(directions[2])) {
				submapCache[index] = submapOffsets[index];
			} else {
				super.fillSubmaps(index);
			}
		}

		@Override
		public long serialized() {
			return isObscured() ? (super.serialized() | (1 << 8)) : super.serialized();
		}
	}
}
